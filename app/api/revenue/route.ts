import {getChatGPTUser} from '@/app/chatgpt-auth';
import {db,json,failure,isAdmin,HttpError,admin,sameOrigin,readJson,validId,field} from '@/lib/server';
export const dynamic='force-dynamic';
export async function GET(){try{
 const user=await getChatGPTUser();if(!user)throw new HttpError(401,'साइन इन करें।');
 const owner=isAdmin(user);const where=owner?'1=1':'c.enabled=1 AND c.producer_email=?';const values=owner?[]:[user.email.trim().toLowerCase()];
 const contracts=await db().prepare(`SELECT c.*,p.song AS title,COALESCE(SUM(l.gross_paise),0) AS gross_paise,COALESCE(SUM(l.deductions_paise),0) AS deductions_paise,COALESCE(SUM(l.producer_paise),0) AS earned_paise,COALESCE(SUM(l.platform_paise),0) AS platform_paise,COALESCE(SUM(l.payout_paise),0) AS paid_paise,COALESCE(SUM(CASE WHEN l.kind='sale' THEN 1 ELSE 0 END),0) AS sales_count FROM revenue_contracts c JOIN performances p ON p.id=c.entry_id LEFT JOIN revenue_ledger l ON l.entry_id=c.entry_id WHERE ${where} GROUP BY c.entry_id ORDER BY c.updated_at DESC`).bind(...values).all();
 const ledger=await db().prepare(`SELECT l.* FROM revenue_ledger l JOIN revenue_contracts c ON c.entry_id=l.entry_id WHERE ${where} ORDER BY l.occurred_at DESC,l.created_at DESC`).bind(...values).all();
 const cleanContracts=contracts.results.map((c:any)=>owner?c:({entry_id:c.entry_id,title:c.title,producer_name:c.producer_name,enabled:c.enabled,producer_percent:c.producer_percent,terms:c.terms,gross_paise:c.gross_paise,deductions_paise:c.deductions_paise,earned_paise:c.earned_paise,paid_paise:c.paid_paise,sales_count:c.sales_count}));
 const cleanLedger=ledger.results.map((l:any)=>owner?l:({id:l.id,entry_id:l.entry_id,kind:l.kind,gross_paise:l.gross_paise,deductions_paise:l.deductions_paise,producer_paise:l.producer_paise,payout_paise:l.payout_paise,producer_percent:l.producer_percent,occurred_at:l.occurred_at}));
 return json({isAdmin:owner,contracts:cleanContracts,ledger:cleanLedger,paymentsConnected:false});
}catch(e){return failure(e)}}
export async function PUT(req:Request){try{
 sameOrigin(req);const user=await admin();const body=await readJson(req);const id=validId(body.entryId||'');
 if(typeof body.enabled!=='boolean')throw new HttpError(400,'Revenue Sharing ON/OFF चुनें।');
 const name=field(body.producerName,'निर्माता का नाम',150);const email=field(body.producerEmail,'लॉगिन ईमेल',254).toLowerCase();if(!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))throw new HttpError(400,'सही लॉगिन ईमेल भरें।');
 if(!Number.isInteger(body.producerPercent)||body.producerPercent<1||body.producerPercent>99)throw new HttpError(400,'निर्माता का हिस्सा 1 से 99 प्रतिशत रखें।');
 const terms=field(body.terms??'','शर्तें',3000,false);const movie=await db().prepare("SELECT id FROM performances WHERE id=? AND content_type='movie' AND status='published'").bind(id).first();if(!movie)throw new HttpError(404,'प्रकाशित फिल्म चुनें।');
 const result=await db().prepare(`INSERT INTO revenue_contracts (entry_id,producer_name,producer_email,enabled,producer_percent,terms,updated_by,updated_at) VALUES (?,?,?,?,?,?,?,?) ON CONFLICT(entry_id) DO UPDATE SET producer_name=excluded.producer_name,producer_email=excluded.producer_email,enabled=excluded.enabled,producer_percent=excluded.producer_percent,terms=excluded.terms,updated_by=excluded.updated_by,updated_at=excluded.updated_at WHERE revenue_contracts.producer_email=excluded.producer_email OR NOT EXISTS(SELECT 1 FROM revenue_ledger WHERE entry_id=excluded.entry_id) RETURNING entry_id`).bind(id,name,email,body.enabled?1:0,body.producerPercent,terms,user.userId,Date.now()).first();
 if(!result)throw new HttpError(409,'हिसाब दर्ज होने के बाद निर्माता का लॉगिन ईमेल नहीं बदल सकते।');return json({saved:true});
}catch(e){return failure(e)}}

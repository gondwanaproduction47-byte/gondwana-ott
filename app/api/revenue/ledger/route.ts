import {admin,db,failure,field,HttpError,json,readJson,sameOrigin,validId} from '@/lib/server';
export async function POST(req:Request){try{
 sameOrigin(req);const user=await admin();const b=await readJson(req);const id=validId(b.entryId||'');const requestId=validId(b.id||'');
 if(!['sale','payout'].includes(b.kind))throw new HttpError(400,'बिक्री या भुगतान चुनें।');
 const reference=field(b.reference,'लेनदेन संदर्भ',120).toLowerCase();const note=field(b.note??'','विवरण',1500,false);
 const amount=(n:unknown)=>typeof n==='number'&&Number.isSafeInteger(n)&&n>=0&&n<=100000000;
 if(!amount(b.grossPaise)||!amount(b.deductionsPaise)||!amount(b.payoutPaise)||!Number.isSafeInteger(b.occurredAt)||b.occurredAt<946684800000||b.occurredAt>Date.now()+60000)throw new HttpError(400,'सही रकम और तारीख भरें।');
 if(b.kind==='sale'&&(b.grossPaise<=0||b.deductionsPaise>b.grossPaise||b.payoutPaise!==0))throw new HttpError(400,'बिक्री और कटौतियों की सही रकम भरें।');
 if(b.kind==='payout'&&(b.payoutPaise<=0||b.grossPaise!==0||b.deductionsPaise!==0))throw new HttpError(400,'भुगतान की सही रकम भरें।');
 if(b.kind==='sale'&&b.deductionsPaise>0&&!note)throw new HttpError(400,'कटौतियों का विवरण भरें।');
 const existing=await db().prepare('SELECT * FROM revenue_ledger WHERE id=? OR reference=?').bind(requestId,reference).first<any>();
 if(existing){if(existing.id===requestId&&existing.entry_id===id&&existing.kind===b.kind&&existing.gross_paise===b.grossPaise&&existing.deductions_paise===b.deductionsPaise&&existing.payout_paise===b.payoutPaise&&existing.reference===reference&&existing.note===note&&existing.occurred_at===b.occurredAt)return json({saved:true});throw new HttpError(409,'यह लेनदेन संदर्भ पहले से दर्ज है।');}
 const net=b.grossPaise-b.deductionsPaise;
 const result=await db().prepare(`INSERT INTO revenue_ledger (id,entry_id,kind,gross_paise,deductions_paise,producer_paise,platform_paise,payout_paise,producer_percent,reference,note,occurred_at,created_by,created_at) SELECT ?,c.entry_id,?,?,?,CAST(ROUND(?*c.producer_percent/100.0) AS INTEGER),?-CAST(ROUND(?*c.producer_percent/100.0) AS INTEGER),?,c.producer_percent,?,?,?,?,? FROM revenue_contracts c WHERE c.entry_id=? AND (?='payout' OR c.enabled=1) AND (?='sale' OR ?<=(SELECT COALESCE(SUM(producer_paise-payout_paise),0) FROM revenue_ledger WHERE entry_id=c.entry_id)) ON CONFLICT DO NOTHING RETURNING id`).bind(requestId,b.kind,b.grossPaise,b.deductionsPaise,net,net,net,b.payoutPaise,reference,note,b.occurredAt,user.userId,Date.now(),id,b.kind,b.kind,b.payoutPaise).first();
 if(!result)throw new HttpError(409,'रिकॉर्ड सेव नहीं हुआ। Sharing ON, बकाया रकम और अलग लेनदेन संदर्भ जाँचें।');return json({saved:true},201);
}catch(e){return failure(e)}}

import {getChatGPTUser} from '@/app/chatgpt-auth';
import {db,json,failure,validId} from '@/lib/server';
import {accessFor,offersQuery,gatewayStatus} from '@/lib/payments';
export async function GET(req:Request,{params}:{params:Promise<{id:string}>}){try{const id=validId((await params).id),a=await accessFor(id,await getChatGPTUser());const offers=a.allowed?[]:(await db().prepare(offersQuery+" AND o.enabled=1 AND ((o.entry_id=? AND o.kind IN ('rent','buy')) OR (?=1 AND o.kind IN ('monthly','yearly'))) ORDER BY o.price_paise").bind(id,a.subscription).all()).results;return json({allowed:a.allowed,offers,gateway:await gatewayStatus()})}catch(e){return failure(e)}}

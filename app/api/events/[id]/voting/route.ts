import {staff,db,failure,HttpError,json,readJson,sameOrigin,validId} from '@/lib/server';
export async function PATCH(req:Request,{params}:{params:Promise<{id:string}>}){
 try{
  sameOrigin(req);await staff('events');const id=validId((await params).id);
  const body=await readJson(req);if(typeof body.enabled!=='boolean')throw new HttpError(400,'वोटिंग चालू या बंद चुनें।');
  const row=await db().prepare('UPDATE events SET voting_enabled=? WHERE id=? RETURNING id,voting_enabled').bind(body.enabled?1:0,id).first();
  if(!row)throw new HttpError(404,'इवेंट नहीं मिला।');return json(row);
 }catch(e){return failure(e)}
}

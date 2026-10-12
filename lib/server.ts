import {env} from 'cloudflare:workers';
import {getChatGPTUser} from '@/app/chatgpt-auth';
export const MAX_SIZE=200*1024*1024, CHUNK_SIZE=8*1024*1024;
export function db(){if(!env.DB)throw new Error('Database unavailable');return env.DB}
export function bucket(){if(!env.BUCKET)throw new Error('Storage unavailable');return env.BUCKET}
export class HttpError extends Error{constructor(public status:number,message:string){super(message)}}
export function json(data:unknown,status=200){return Response.json(data,{status,headers:{'Cache-Control':'no-store','X-Content-Type-Options':'nosniff'}})}
export function failure(error:unknown){if(error instanceof HttpError)return json({error:error.message},error.status);console.error('Singing event request failed',error instanceof Error?error.message:'unknown');return json({error:'अभी अनुरोध पूरा नहीं हुआ। थोड़ी देर बाद फिर कोशिश करें।'},503)}
export function sameOrigin(req:Request){const origin=req.headers.get('origin');const allowed=env.PUBLIC_ORIGIN||new URL(req.url).origin;if(!origin||origin!==allowed)throw new HttpError(403,'इस ऐप से ही अनुरोध करें।')}
export async function signedIn(){const user=await getChatGPTUser();if(!user)throw new HttpError(401,'वोट देने के लिए साइन इन करें।');return user}
export function isAdmin(user:{email:string}|null){return !!user&&!!env.OWNER_EMAIL&&user.email.toLowerCase()===env.OWNER_EMAIL.toLowerCase()}
export async function admin(){const user=await signedIn();if(!isAdmin(user))throw new HttpError(403,'वीडियो केवल आयोजक अपलोड कर सकते हैं।');return user}
export async function readJson(req:Request){if(!req.headers.get('content-type')?.includes('application/json'))throw new HttpError(415,'JSON आवश्यक है।');if(Number(req.headers.get('content-length')||0)>10000)throw new HttpError(413,'बहुत बड़ा अनुरोध।');const text=await req.text();if(text.length>10000)throw new HttpError(413,'बहुत बड़ा अनुरोध।');try{return JSON.parse(text)}catch{throw new HttpError(400,'अनुरोध सही नहीं है।')}}
export function validId(id:string){if(!/^[a-f0-9-]{36}$/.test(id))throw new HttpError(404,'परफ़ॉर्मेंस नहीं मिली।');return id}
export function field(value:unknown,name:string,max:number,required=true){if(typeof value!=='string'||value.trim().length>max||(required&&!value.trim()))throw new HttpError(400,`${name} सही भरें।`);return value.trim()}
export type Entry={id:string;singer:string;song:string;city:string;object_key:string;mime:string;size:number;upload_id:string;status:string;created_by:string;created_at:number};
export async function uploadEntry(id:string,userId:string){validId(id);const item=await db().prepare('SELECT * FROM performances WHERE id = ? AND created_by = ?').bind(id,userId).first<Entry>();if(!item)throw new HttpError(404,'अपलोड नहीं मिला।');if(item.status!=='published'&&item.created_at<Date.now()-86400000)throw new HttpError(410,'अपलोड की अवधि समाप्त हो गई। नई वीडियो अपलोड करें।');return item}
export async function permissions(user:{email:string}|null){const owner=isAdmin(user);const row=!owner&&user?await db().prepare('SELECT role FROM team_members WHERE email=?').bind(user.email.trim().toLowerCase()).first<{role:string}>():null;return {isAdmin:owner,isStaff:owner||!!row,canUpload:owner||row?.role==='uploader',canEvents:owner||row?.role==='event_manager',role:owner?'owner':row?.role||'viewer'}}
export async function staff(capability:'upload'|'events'){const user=await signedIn();const p=await permissions(user);if(!(capability==='upload'?p.canUpload:p.canEvents))throw new HttpError(403,'आपके खाते को यह अधिकार नहीं है।');return user}

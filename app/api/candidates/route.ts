import {getChatGPTUser} from '@/app/chatgpt-auth';
import {db,json,failure,sameOrigin,staff,permissions,readJson,field,validId,HttpError} from '@/lib/server';
export async function GET(req:Request){try{
 const eventId=validId(new URL(req.url).searchParams.get('eventId')||'');const u=await getChatGPTUser(),p=await permissions(u),now=Date.now();
 const rows=await db().prepare(`SELECT c.id,c.event_id,c.episode_group_id,c.name,c.city,c.number,c.photo_key IS NOT NULL AS has_photo,eg.title AS round_title,eg.season,eg.episode,
 EXISTS(SELECT 1 FROM candidate_votes v WHERE v.candidate_id=c.id AND v.user_id=?) AS voted,
 EXISTS(SELECT 1 FROM candidate_votes v WHERE v.scope_key=c.scope_key AND v.user_id=?) AS scope_voted,
 (e.candidate_voting=1 AND e.voting_enabled=1 AND e.starts_at<=? AND e.ends_at>? AND (c.episode_group_id IS NULL OR eg.voting_enabled=1) AND (e.episode_mode=0 OR eg.id IS NOT NULL)) AS can_vote
 ${p.canEvents?',(SELECT COUNT(*) FROM candidate_votes v WHERE v.candidate_id=c.id) AS votes':''}
 FROM candidates c JOIN events e ON e.id=c.event_id LEFT JOIN episode_groups eg ON eg.id=c.episode_group_id AND eg.event_id=c.event_id WHERE c.event_id=? ORDER BY COALESCE(eg.season,0),COALESCE(eg.episode,0),c.number`).bind(u?.userId||'',u?.userId||'',now,now,eventId).all();return json({candidates:rows.results});
}catch(e){return failure(e)}}
export async function POST(req:Request){try{sameOrigin(req);await staff('events');const b=await readJson(req),eventId=validId(b.eventId),group=b.episodeGroupId?validId(b.episodeGroupId):null;
 const event=await db().prepare('SELECT id FROM events WHERE id=?').bind(eventId).first();if(!event)throw new HttpError(404,'इवेंट नहीं मिला।');
 if(group&&!await db().prepare('SELECT id FROM episode_groups WHERE id=? AND event_id=?').bind(group,eventId).first())throw new HttpError(400,'इस इवेंट का राउंड चुनें।');
 const name=field(b.name,'उम्मीदवार का नाम',120),city=field(b.city||'','शहर',100,false),number=Number(b.number);if(!Number.isSafeInteger(number)||number<1||number>99999)throw new HttpError(400,'सही क्रमांक भरें।');const scope=group||eventId,id=crypto.randomUUID();
 const result=await db().prepare('INSERT INTO candidates (id,event_id,episode_group_id,scope_key,name,city,number,created_at) VALUES (?,?,?,?,?,?,?,?) ON CONFLICT(scope_key,number) DO NOTHING RETURNING id').bind(id,eventId,group,scope,name,city,number,Date.now()).first();if(!result)throw new HttpError(409,'इस राउंड में यह क्रमांक पहले से है।');return json({id},201);
}catch(e){return failure(e)}}
export async function PATCH(req:Request){try{sameOrigin(req);await staff('events');const b=await readJson(req);
 if(b.action==='mode'){const id=validId(b.eventId);if(typeof b.enabled!=='boolean')throw new HttpError(400,'ON / OFF चुनें।');const row=await db().prepare('UPDATE events SET candidate_voting=? WHERE id=? RETURNING id').bind(b.enabled?1:0,id).first();if(!row)throw new HttpError(404,'इवेंट नहीं मिला।');}
 else if(b.action==='assign'){const id=validId(b.candidateId),entryId=validId(b.entryId);const row=await db().prepare(`UPDATE performances SET candidate_id=? WHERE id=? AND status='published' AND EXISTS(SELECT 1 FROM candidates c WHERE c.id=? AND c.event_id=performances.event_id AND c.episode_group_id IS performances.episode_group_id) RETURNING id`).bind(id,entryId,id).first();if(!row)throw new HttpError(400,'उम्मीदवार और वीडियो एक ही इवेंट व राउंड के होने चाहिए।');}
 else if(b.action==='edit'){const id=validId(b.id);const row=await db().prepare('UPDATE candidates SET name=?,city=? WHERE id=? RETURNING id').bind(field(b.name,'नाम',120),field(b.city||'','शहर',100,false),id).first();if(!row)throw new HttpError(404,'उम्मीदवार नहीं मिला।');}
 else throw new HttpError(400,'सही विकल्प चुनें।');return json({saved:true});
}catch(e){return failure(e)}}

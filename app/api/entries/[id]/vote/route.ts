import {db,json,failure,sameOrigin,signedIn,validId,HttpError,permissions} from '@/lib/server';
export async function POST(req:Request,{params}:{params:Promise<{id:string}>}){try{
 sameOrigin(req);const user=await signedIn();const id=validId((await params).id);
 const exists=await db().prepare("SELECT id FROM performances WHERE id=? AND status='published' AND content_type IN ('singing','dance','other')").bind(id).first();
 if(!exists)throw new HttpError(404,'परफ़ॉर्मेंस उपलब्ध नहीं है।');
 const now=Date.now();
 const result=await db().prepare(`INSERT INTO votes (entry_id,user_id,created_at) SELECT p.id,?,? FROM performances p JOIN events e ON e.id=p.event_id LEFT JOIN episode_groups eg ON eg.id=p.episode_group_id AND eg.event_id=e.id WHERE p.id=? AND p.status='published' AND p.content_type IN ('singing','dance','other') AND p.vote_eligible=1 AND e.candidate_voting=0 AND e.voting_enabled=1 AND (p.episode_group_id IS NULL OR eg.voting_enabled=1) AND (e.episode_mode=0 OR eg.id IS NOT NULL) AND e.starts_at<=? AND e.ends_at>? ON CONFLICT (entry_id,user_id) DO UPDATE SET created_at=votes.created_at RETURNING entry_id`).bind(user.userId,now,id,now,now).first();
 if(!result)throw new HttpError(403,'इस इवेंट की वोटिंग अभी बंद है।');
 if(!(await permissions(user)).canEvents)return json({id,voted:1});const row=await db().prepare('SELECT COUNT(*) AS votes FROM votes WHERE entry_id=?').bind(id).first<{votes:number}>();return json({id,votes:row?.votes||0,voted:1});
}catch(e){return failure(e)}}

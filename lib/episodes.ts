import {db,field,HttpError,validId} from './server';
export async function resolveEpisode(body:any,contentType:string,eventId:string|null){
 const description=field(body.description??'','विवरण',2000,false);const duration=body.durationSeconds??0;
 if(!Number.isInteger(duration)||duration<0||duration>86400)throw new HttpError(400,'अवधि सही भरें।');
 const eligible=body.voteEligible??true;if(typeof eligible!=='boolean')throw new HttpError(400,'वीडियो का प्रकार सही चुनें।');
 let group:any=null;if(body.episodeGroupId){const id=validId(body.episodeGroupId);group=await db().prepare('SELECT * FROM episode_groups WHERE id=?').bind(id).first();if(!group|| (contentType==='series'?!!group.event_id||group.series_title!==String(body.seriesTitle||'').trim():!eventId||group.event_id!==eventId))throw new HttpError(400,'इसी सीरीज़ / इवेंट का एपिसोड चुनें।');}
 if(eventId){const event=await db().prepare('SELECT episode_mode FROM events WHERE id=?').bind(eventId).first<any>();if(event?.episode_mode&&!group)throw new HttpError(400,'इस इवेंट के लिए एपिसोड चुनना आवश्यक है।');}
 return {id:group?.id||null,season:group?.season,episode:group?.episode,description:description||group?.description||'',duration:duration||group?.duration_seconds||0,eligible:eligible?1:0};
}

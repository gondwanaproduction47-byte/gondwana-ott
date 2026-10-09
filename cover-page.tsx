'use client';
import {useState,type ComponentProps} from 'react';
import {Play,Film,Mic2,Music2,Clapperboard} from 'lucide-react';
import EventApp from './event-app';
import {Button} from '@/components/ui/button';
export default function CoverPage(props:ComponentProps<typeof EventApp>){
 const [entered,setEntered]=useState(false);
 if(entered)return <EventApp {...props}/>;
 return <main className="coming-cover"><div className="cover-top"><span className="cover-brand">GONDWANA <b>OTT</b></span><span className="cover-production">Gondwana Production</span></div><section className="cover-content" aria-labelledby="coming-title"><p className="cover-kicker">फिल्में · कहानियाँ · आपका मंच</p><h1 id="coming-title">COMING<br/><span>SOON</span></h1><p className="cover-hindi">मनोरंजन का नया सफ़र, जल्द आ रहा है।</p><div className="cover-categories"><span><Film size={18}/>फिल्में</span><span><Clapperboard size={18}/>वेब सीरीज़</span><span><Mic2 size={18}/>सिंगिंग</span><span><Music2 size={18}/>डांस</span></div><article className="film-announcement" aria-label="जंगल सत्याग्रह फिल्म बैनर"><div className="film-announcement-top"><span>GONDWANA PRODUCTION</span><span>COMING SOON</span></div><h2>जंगल सत्याग्रह <span>(1930)</span></h2><p className="film-announcement-subtitle">THE UNTOLD HISTORY</p><p className="film-announcement-platform">जल्द · GONDWANA OTT पर</p></article><Button className="cover-enter" onClick={()=>setEntered(true)}><Play size={19} fill="currentColor"/>ऐप खोलें</Button></section><footer className="cover-bottom"><span>GONDWANA OTT</span><span>जल्द मिलते हैं।</span></footer></main>
}

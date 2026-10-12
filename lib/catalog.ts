export const sections = [
 {id:'singing',label:'सिंगिंग इवेंट',title:'सिंगिंग इवेंट',description:'गायकी देखें और अपने पसंदीदा गायक को वोट दें।'},
 {id:'dance',label:'डांस इवेंट',title:'डांस इवेंट',description:'डांस परफ़ॉर्मेंस देखें और अपने पसंदीदा कलाकार को वोट दें।'},
 {id:'other',label:'अन्य इवेंट',title:'इवेंट',description:'इवेंट की परफ़ॉर्मेंस देखें।'},
 {id:'movie',label:'फिल्में',title:'फिल्में',description:'अपनी पसंद की फिल्में देखें।'},
 {id:'series',label:'वेब सीरीज़',title:'वेब सीरीज़',description:'सीरीज़ चुनें और सीज़न के अनुसार एपिसोड देखें।'}
] as const;
export type ContentType = typeof sections[number]['id'];
export const genres = ['एक्शन','ड्रामा','कॉमेडी','ऐतिहासिक'] as const;
export function displayGenre(genre:string){if(['एक्शन','थ्रिलर','हॉरर','मिस्ट्री','क्राइम','एडवेंचर','साइंस फिक्शन','फैंटेसी'].includes(genre))return 'एक्शन';if(['ऐतिहासिक','बायोग्राफी','डॉक्यूमेंट्री'].includes(genre))return 'ऐतिहासिक';if(genre==='कॉमेडी')return 'कॉमेडी';return 'ड्रामा';}
export const isEvent = (type:string) => type==='singing'||type==='dance'||type==='other';

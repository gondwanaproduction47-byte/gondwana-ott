import type {Metadata,Viewport} from 'next';
import './globals.css';
import './video-shelves.css';
import './revenue.css';
export const metadata:Metadata={title:'Gondwana OTT | सिंगिंग, डांस, फिल्में और वेब सीरीज़',description:'सिंगिंग और डांस इवेंट में वोट दें। अपनी पसंद की फिल्में और वेब सीरीज़ देखें।',manifest:'/manifest.webmanifest',icons:{icon:'/favicon.svg',shortcut:'/favicon.svg'}};
export const viewport:Viewport={width:'device-width',initialScale:1,themeColor:'#110f16'};
export default function Layout({children}:{children:React.ReactNode}){return <html lang="hi" className="dark"><body>{children}</body></html>}

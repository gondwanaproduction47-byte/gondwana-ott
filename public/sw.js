// Keep private page responses out of offline storage. Online access requires sign-in.
self.addEventListener('install',()=>self.skipWaiting());self.addEventListener('activate',event=>event.waitUntil(self.clients.claim()));self.addEventListener('fetch',()=>{});

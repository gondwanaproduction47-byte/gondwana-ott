import {createRequire} from 'node:module';
import {readFile,readdir} from 'node:fs/promises';
import {createHmac,randomUUID} from 'node:crypto';
import assert from 'node:assert/strict';
const require=createRequire(import.meta.url),wr=createRequire(require.resolve('wrangler')),{Miniflare}=wr('miniflare');
const root=new URL('../dist/server/',import.meta.url).pathname,files=(await readdir(root,{recursive:true})).filter(x=>/\.m?js$/.test(x)&&x!=='index.js');
 const remoteOrders=new Map(),remotePayments=new Map();let calls=0;
const mf=new Miniflare({modules:['index.js',...files].map(x=>({type:'ESModule',path:root+x})),compatibilityDate:'2026-05-15',compatibilityFlags:['nodejs_compat'],d1Databases:['DB'],r2Buckets:['BUCKET'],cf:false,bindings:{OWNER_EMAIL:'owner@test.invalid',PUBLIC_ORIGIN:'https://test.invalid',RAZORPAY_KEY_ID:'rzp_live_localfixture',RAZORPAY_KEY_SECRET:'test-secret-only',RAZORPAY_WEBHOOK_SECRET:'test-webhook-only',PAYMENTS_ENABLED:'true',PAYMENT_SETTINGS_KEY:Buffer.alloc(32,7).toString('base64')},outboundService:async request=>{
 const u=new URL(request.url);assert.equal(u.origin,'https://api.razorpay.com');assert.equal(request.headers.get('authorization'),'Basic '+Buffer.from('rzp_live_localfixture:test-secret-only').toString('base64'));
 if(u.pathname==='/v1/orders'&&request.method==='POST'){const b=await request.json();assert.equal(b.currency,'INR');assert.equal(b.partial_payment,false);const o={...b,id:'order_Test'+(++calls)};remoteOrders.set(o.id,o);return Response.json(o)}
 const pm=/^\/v1\/payments\/(pay_\w+)$/.exec(u.pathname);if(pm)return Response.json(remotePayments.get(pm[1]));
 const om=/^\/v1\/orders\/(order_\w+)\/payments$/.exec(u.pathname);if(om)return Response.json({items:[...remotePayments.values()].filter(p=>p.order_id===om[1])});
 throw new Error('Unexpected provider request '+u.pathname);
}});
try{
 const db=await mf.getD1Database('DB');for(const name of (await readdir(new URL('../drizzle/',import.meta.url))).filter(n=>n.endsWith('.sql')).sort()){for(const s of (await readFile(new URL('../drizzle/'+name,import.meta.url),'utf8')).split('--> statement-breakpoint'))if(s.trim())await db.prepare(s).run()}
 const owner={'oai-authenticated-user-id':'owner','oai-authenticated-user-email':'owner@test.invalid'},viewer={'oai-authenticated-user-id':'viewer','oai-authenticated-user-email':'viewer@test.invalid'},other={'oai-authenticated-user-id':'other','oai-authenticated-user-email':'other@test.invalid'};
 const send=(path,options={})=>mf.dispatchFetch('https://test.invalid'+path,options),api=(path,method,body,user=owner)=>send(path,{method,headers:{Origin:'https://test.invalid','Content-Type':'application/json',...user},body:JSON.stringify(body)});
 for(const role of ['uploader','event_manager'])await db.prepare('INSERT INTO team_members(email,role,updated_at) VALUES (?,?,?)').bind(role+'@test.invalid',role,Date.now()).run();
 for(const user of [viewer,other,{'oai-authenticated-user-id':'uploader','oai-authenticated-user-email':'uploader@test.invalid'},{'oai-authenticated-user-id':'event_manager','oai-authenticated-user-email':'event_manager@test.invalid'}]){let deny=await send('/api/payment-settings',{headers:user});assert.equal(deny.status,403);deny=await api('/api/payment-settings','PUT',{section:'upi',upiId:'bad@bank',payeeName:'Bad',enabled:true},user);assert.equal(deny.status,403);}
 let setup=await api('/api/payment-settings','PUT',{section:'gateway',keyId:'rzp_live_localfixture',secret:'test-secret-only',webhook:'test-webhook-only',enabled:true});assert.equal(setup.status,200,await setup.clone().text());
 const savedConfig=await db.prepare('SELECT gateway_encrypted FROM payment_settings WHERE id=1').first();assert.ok(savedConfig.gateway_encrypted);assert.ok(!savedConfig.gateway_encrypted.includes('test-secret-only'));
 setup=await send('/api/payment-settings',{headers:owner});const safeSettings=await setup.json();assert.equal(safeSettings.ready,true);assert.ok(!Object.hasOwn(safeSettings,'secret')&&!Object.hasOwn(safeSettings,'gateway_encrypted'));
 for(const user of [viewer,other]){const pr=await api('/api/profile','PUT',{name:user===viewer?'Test Viewer':'Other Viewer',mobile:'9876543210',city:'Nagpur'},user);assert.equal(pr.status,200)}
 setup=await api('/api/profile','PUT',{name:'Bad',mobile:'123',city:'City'},viewer);assert.equal(setup.status,400);
 const movie=randomUUID(),movie2=randomUUID();for(const id of [movie,movie2]){await db.prepare("INSERT INTO performances(id,singer,song,content_type,object_key,mime,size,upload_id,status,created_by,created_at,published_at) VALUES (?,?,?,'movie',?,'video/mp4',8,?,'published','owner',?,?)").bind(id,'Producer','Test Movie',id,id,Date.now(),Date.now()).run();await(await mf.getR2Bucket('BUCKET')).put(id,new Uint8Array(8))}
 let r=await send('/api/media/'+movie);assert.equal(r.status,200,'existing content stays free');
 r=await api('/api/offers','PATCH',{action:'access',entryId:movie,paid:true,subscription:false},viewer);assert.equal(r.status,403);
 r=await api('/api/offers','PATCH',{action:'access',entryId:movie,paid:true,subscription:false});assert.equal(r.status,200);
 r=await send('/api/media/'+movie);assert.equal(r.status,401);r=await send('/api/media/'+movie,{headers:viewer});assert.equal(r.status,402);r=await api('/api/playback','POST',{entryId:movie},viewer);assert.equal(r.status,402);
 r=await send('/api/media/'+movie,{headers:owner});assert.equal(r.status,200);
 r=await api('/api/offers','PUT',{entryId:movie,kind:'rent',pricePaise:9900,durationDays:2});assert.equal(r.status,201);const rent=await r.json();
 const requestId=randomUUID();r=await api('/api/orders','POST',{offerId:rent.id,requestId,amount:1,kind:'buy'},viewer);assert.equal(r.status,201,await r.clone().text());const order=await r.json();assert.equal(order.amount,9900);assert.equal(calls,1);
 r=await api('/api/orders','POST',{offerId:rent.id,requestId:randomUUID()},viewer);assert.equal(r.status,201);assert.equal((await r.json()).orderId,order.orderId);assert.equal(calls,1,'reuse pending order rather than charge twice');
 const pay={id:'pay_Test1',order_id:order.orderId,amount:9900,currency:'INR',captured:false,status:'authorized',amount_refunded:0};remotePayments.set(pay.id,pay);
 const signature=createHmac('sha256','test-secret-only').update(order.orderId+'|'+pay.id).digest('hex'),verify={id:order.id,razorpay_order_id:order.orderId,razorpay_payment_id:pay.id,razorpay_signature:signature};
 r=await api('/api/payments/verify','POST',{...verify,razorpay_signature:'0'.repeat(64)},viewer);assert.equal(r.status,400);
 r=await api('/api/payments/verify','POST',verify,other);assert.equal(r.status,404);
 r=await api('/api/payments/verify','POST',verify,viewer);assert.equal(r.status,200);assert.equal((await r.json()).status,'pending','authorized is not captured');
 pay.status='captured';pay.captured=true;pay.amount=1;r=await api('/api/payments/verify','POST',verify,viewer);assert.equal(r.status,400);pay.amount=9900;
 r=await api('/api/payments/verify','POST',verify,viewer);assert.equal(r.status,200);assert.equal((await r.json()).status,'paid');
 let row=await db.prepare('SELECT * FROM payment_orders WHERE id=?').bind(order.id).first();const expiry=row.expires_at;assert.equal(expiry-row.paid_at,2*86400000);
 r=await send('/api/media/'+movie,{headers:viewer});assert.equal(r.status,200);r=await send('/api/media/'+movie,{headers:other});assert.equal(r.status,402);
 const webhook=async(event,payment=pay,valid=true)=>{const raw=JSON.stringify({event,payload:{payment:{entity:payment}}});return send('/api/payments/webhook',{method:'POST',headers:{'x-razorpay-signature':valid?createHmac('sha256','test-webhook-only').update(raw).digest('hex'):'0'.repeat(64)},body:raw})};
 r=await webhook('payment.captured',pay,false);assert.equal(r.status,400);r=await webhook('payment.captured');assert.equal(r.status,200);row=await db.prepare('SELECT expires_at FROM payment_orders WHERE id=?').bind(order.id).first();assert.equal(row.expires_at,expiry,'duplicate callback does not extend rental');
 await db.prepare('UPDATE payment_orders SET expires_at=? WHERE id=?').bind(Date.now()-1,order.id).run();r=await send('/api/media/'+movie,{headers:viewer});assert.equal(r.status,402);
 r=await api('/api/offers','PUT',{kind:'monthly',pricePaise:19900});assert.equal(r.status,201);const monthly=await r.json();r=await api('/api/offers','PUT',{kind:'yearly',pricePaise:99900});assert.equal(r.status,201);const yearly=await r.json();
 r=await api('/api/orders','POST',{offerId:monthly.id,requestId:randomUUID()},viewer);assert.equal(r.status,201);const sub=await r.json();assert.equal(sub.amount,19900,'exact offer chosen, not newest plan');
 const subpay={id:'pay_Sub1',order_id:sub.orderId,amount:19900,currency:'INR',captured:true,status:'captured',amount_refunded:0};remotePayments.set(subpay.id,subpay);r=await webhook('order.paid',subpay);assert.equal(r.status,200);
 row=await db.prepare('SELECT * FROM payment_orders WHERE id=?').bind(sub.id).first();assert.equal(row.expires_at-row.paid_at,30*86400000);
 r=await send('/api/media/'+movie,{headers:viewer});assert.equal(r.status,402,'subscription excludes rent-only movies');
 await api('/api/offers','PATCH',{action:'access',entryId:movie,paid:true,subscription:true});r=await send('/api/media/'+movie,{headers:viewer});assert.equal(r.status,200);
 subpay.amount_refunded=19900;subpay.status='refunded';r=await webhook('refund.processed',subpay);assert.equal(r.status,200);r=await send('/api/media/'+movie,{headers:viewer});assert.equal(r.status,402);
 r=await webhook('payment.captured',subpay);assert.equal(r.status,200);row=await db.prepare('SELECT status FROM payment_orders WHERE id=?').bind(sub.id).first();assert.equal(row.status,'refunded');
 r=await api('/api/offers','PUT',{entryId:movie,kind:'buy',pricePaise:49900});assert.equal(r.status,201);const buy=await r.json();r=await api('/api/orders','POST',{offerId:buy.id,requestId:randomUUID()},viewer);const purchase=await r.json();assert.equal(r.status,201);
 const bp={id:'pay_Buy1',order_id:purchase.orderId,amount:49900,currency:'INR',captured:true,status:'captured',amount_refunded:0};remotePayments.set(bp.id,bp);r=await api('/api/orders','PATCH',{id:purchase.id},viewer);assert.equal(r.status,200);assert.equal((await r.json()).status,'paid');row=await db.prepare('SELECT expires_at FROM payment_orders WHERE id=?').bind(purchase.id).first();assert.equal(row.expires_at,null);
 r=await send('/api/account',{headers:other});assert.deepEqual((await r.json()).orders,[]);r=await send('/api/orders',{headers:viewer});assert.equal(r.status,403);r=await send('/api/offers',{headers:viewer});const publicData=await r.json();assert.equal(Object.hasOwn(publicData.gateway,'secret'),false);assert.deepEqual(publicData.access,[]);
 await api('/api/offers','PATCH',{id:yearly.id,enabled:false});r=await api('/api/orders','POST',{offerId:yearly.id,requestId:randomUUID()},other);assert.equal(r.status,404);
 await api('/api/offers','PATCH',{action:'access',entryId:movie2,paid:true,subscription:false});r=await api('/api/offers','PUT',{entryId:movie2,kind:'rent',pricePaise:10000,durationDays:7});const manualOffer=await r.json();assert.equal(r.status,201);
 r=await api('/api/orders','POST',{offerId:manualOffer.id,requestId:randomUUID(),method:'manual'},other);assert.equal(r.status,503);
 r=await api('/api/payment-settings','PUT',{section:'upi',upiId:'merchant@bank',payeeName:'Gondwana',enabled:true});assert.equal(r.status,200);
 r=await api('/api/orders','POST',{offerId:manualOffer.id,requestId:randomUUID(),method:'manual'},other);assert.equal(r.status,201,await r.clone().text());const manual=await r.json();
 r=await send('/api/manual-payment?id='+manual.id,{headers:viewer});assert.equal(r.status,404);
 r=await api('/api/payment-settings','PUT',{section:'upi',upiId:'newmerchant@bank',payeeName:'New payee',enabled:true});assert.equal(r.status,200);
 r=await send('/api/manual-payment?id='+manual.id,{headers:other});assert.equal((await r.json()).order.upi_id,'merchant@bank','existing order keeps original payee');
 r=await api('/api/manual-payment','POST',{id:manual.id,utr:'123456789012'},viewer);assert.equal(r.status,409);
 r=await api('/api/manual-payment','POST',{id:manual.id,utr:'123'},other);assert.equal(r.status,400);
 r=await api('/api/manual-payment','POST',{id:manual.id,utr:'123456789012'},other);assert.equal(r.status,200);
 r=await send('/api/media/'+movie2,{headers:other});assert.equal(r.status,402,'UTR alone grants no access');
 r=await api('/api/orders','POST',{offerId:manualOffer.id,requestId:randomUUID(),method:'manual'},viewer);assert.equal(r.status,201);const secondManual=await r.json();
 r=await api('/api/manual-payment','POST',{id:secondManual.id,utr:'123456789012'},viewer);assert.equal(r.status,409,'duplicate UTR blocked across users');
 r=await api('/api/manual-payment','POST',{id:secondManual.id,utr:'123456789013'},viewer);assert.equal(r.status,200);
 r=await api('/api/manual-payment','PATCH',{id:manual.id,action:'approve',amountPaise:10000,confirmed:true},viewer);assert.equal(r.status,403);
 r=await api('/api/manual-payment','PATCH',{id:manual.id,action:'approve',amountPaise:1,confirmed:true});assert.equal(r.status,400);
 r=await api('/api/manual-payment','PATCH',{id:manual.id,action:'approve',amountPaise:10000,confirmed:false});assert.equal(r.status,400);
 r=await api('/api/manual-payment','PATCH',{id:manual.id,action:'approve',amountPaise:10000,confirmed:true});assert.equal(r.status,200);let approved=await db.prepare('SELECT * FROM payment_orders WHERE id=?').bind(manual.id).first();assert.equal(approved.expires_at-approved.paid_at,7*86400000);assert.equal(approved.customer_name,'Other Viewer');
 r=await api('/api/manual-payment','PATCH',{id:manual.id,action:'approve',amountPaise:10000,confirmed:true});assert.equal(r.status,200);assert.equal((await db.prepare('SELECT expires_at FROM payment_orders WHERE id=?').bind(manual.id).first()).expires_at,approved.expires_at);
 r=await send('/api/media/'+movie2,{headers:other});assert.equal(r.status,200);
 r=await api('/api/manual-payment','PATCH',{id:secondManual.id,action:'reject',note:'Bank reference not found'});assert.equal(r.status,200);r=await send('/api/media/'+movie2,{headers:viewer});assert.equal(r.status,402);
 r=await api('/api/manual-payment','PATCH',{id:secondManual.id,action:'approve',amountPaise:10000,confirmed:true});assert.equal(r.status,409);
 r=await send('/api/orders',{headers:owner});const report=await r.json();assert.equal(report.orders.find(o=>o.id===manual.id).customer_mobile,'9876543210');assert.equal(report.orders.find(o=>o.id===manual.id).utr,'123456789012');
 console.log('PASS: gateway signature/capture checks, paid access, owner-only encrypted settings, customer profiles, payee snapshots, UTR uniqueness, no access before manual approval, exact amount confirmation, rejection and idempotent approval. No real payments.');
}finally{await mf.dispose()}

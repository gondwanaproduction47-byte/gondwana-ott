import {getChatGPTUser,chatGPTSignInPath,chatGPTSignOutPath} from '@/app/chatgpt-auth';
import {permissions,json,failure} from '@/lib/server';
export const dynamic='force-dynamic';
export async function GET(){try{const u=await getChatGPTUser();return json({signedIn:!!u,...(await permissions(u)),name:u?.fullName|| (u?'दर्शक':''),signInUrl:chatGPTSignInPath('/'),signOutUrl:chatGPTSignOutPath('/')})}catch(e){return failure(e)}}

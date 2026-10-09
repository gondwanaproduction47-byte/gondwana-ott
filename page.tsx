import CoverPage from './cover-page';
import {getChatGPTUser,chatGPTSignInPath,chatGPTSignOutPath} from './chatgpt-auth';
import {permissions} from '@/lib/server';
export const dynamic='force-dynamic';
export default async function Page(){const u=await getChatGPTUser();return <CoverPage session={{signedIn:!!u,...(await permissions(u)),name:u?.fullName||(u?'दर्शक':''),signInUrl:chatGPTSignInPath('/'),signOutUrl:chatGPTSignOutPath('/')}}/>}

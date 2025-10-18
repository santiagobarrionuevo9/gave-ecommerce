import { LoginReq } from "./loginreq";

export interface RegisterReq extends LoginReq { 
    fullName?: string; 
    role?: 'ADMIN'|'CLIENT'; 
}

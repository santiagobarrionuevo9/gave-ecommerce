export interface RegisterReq  { 
email: string;
  password: string;
  fullName: string;
  role?: 'ADMIN' | 'CLIENT';
}

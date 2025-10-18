export interface AuthResponse { 
    token: string; 
    email: string; 
    role: 'ADMIN'|'CLIENT'; 
}
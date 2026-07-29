import React from "react";
import LoginForm from "../components/LoginForm";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthProvider";

export default function LoginPage() {
    const auth= useAuth();
    if(auth.isAuthenticated){
        return <Navigate to="/dashboard"/>
    }
    return (
        <LoginForm />
    );
}
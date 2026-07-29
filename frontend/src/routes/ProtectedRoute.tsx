//Esto solo sirve si es que el usuario esta en otra ruta te redirija a la página donde quieras que sea púcblica, de etsa forma es la que funciona una
//ruta protegida, 
import { Outlet, Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthProvider";
export default function ProtectedRoute(){
    /*const[isAuth, setIsAuth] = useState(false); ///Cómo ya sabemos el useState permite actualizar las variables, en esta caso parte de esto, caso contrario otro
*/
    const auth = useAuth();

    //return isAuth   ? <Outlet /> : <Navigate to="/"/>;
    if (auth.isAuthenticated === undefined) {
        return <div>Loading...</div>;
    }

    return auth.isAuthenticated ? <Outlet /> : <Navigate to="/login" />;
}
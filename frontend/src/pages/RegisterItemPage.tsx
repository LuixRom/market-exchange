import ItemForm from "../components/ItemForm";
import { ItemResponse } from "../interfaces/item/ItemResponse";

function RegisterItemPage() {
    const initialData = {
        name: "",
        description: "",
        condition: "NEW" as "NEW" | "USED",
    };


    function handleSuccess(response: ItemResponse) {
        console.log("Ítem registrado:", response);
    }

    function handleError(error: unknown) {
        console.error("Error al registrar ítem:", error);
    }

    return (
        <div>
            <ItemForm
                initialData={initialData}
                onSubmitSuccess={handleSuccess}
                onSubmitError={handleError}
            />
        </div>
    );
}

export default RegisterItemPage;

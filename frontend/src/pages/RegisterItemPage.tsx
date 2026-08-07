import ItemForm from "../components/ItemForm";
import { ItemResponse } from "../interfaces/item/ItemResponse";

function handleSuccess(response: ItemResponse) {
    console.log("Ítem registrado:", response);
}

function handleError(error: unknown) {
    console.error("Error al registrar ítem:", error);
}

function RegisterItemPage() {
    return (
        <div>
            <ItemForm
                onSubmitSuccess={handleSuccess}
                onSubmitError={handleError}
            />
        </div>
    );
}

export default RegisterItemPage;

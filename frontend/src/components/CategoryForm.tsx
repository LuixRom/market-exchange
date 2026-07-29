import { ChangeEvent, FormEvent } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { CategoryRequest } from "../interfaces/category/CategoryRequest";
import { Card } from "./ui/Card";
import { Input } from "./ui/Input";
import { Button } from "./ui/Button";
import { slideUp } from "../lib/motion";

interface CategoryFormProps {
    title: string;
    formData: CategoryRequest;
    setFormData: React.Dispatch<React.SetStateAction<CategoryRequest>>;
    onSubmit: (e: FormEvent<HTMLFormElement>) => void;
    submitLabel: string;
    successMessage?: string | null;
    errorMessage?: string | null;
}

export default function CategoryForm({
    title,
    formData,
    setFormData,
    onSubmit,
    submitLabel,
    successMessage,
    errorMessage,
}: CategoryFormProps) {
    function handleChange(e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
    }

    return (
        <div className="flex justify-center items-center min-h-screen py-10">
            <motion.div
                className="w-full max-w-md px-4"
                variants={slideUp}
                initial="hidden"
                animate="visible"
                transition={{ duration: 0.3 }}
            >
                <Card className="p-8 border-0 shadow-lg">
                    <h1 className="text-2xl font-bold mb-6 text-center text-gray-900">{title}</h1>

                    <AnimatePresence>
                        {successMessage && (
                            <motion.p
                                className="text-success text-sm mb-4 text-center"
                                initial={{ opacity: 0, height: 0 }}
                                animate={{ opacity: 1, height: "auto" }}
                                exit={{ opacity: 0, height: 0 }}
                                transition={{ duration: 0.2 }}
                            >
                                {successMessage}
                            </motion.p>
                        )}
                        {errorMessage && (
                            <motion.p
                                className="text-danger text-sm mb-4 text-center"
                                initial={{ opacity: 0, height: 0 }}
                                animate={{ opacity: 1, height: "auto" }}
                                exit={{ opacity: 0, height: 0 }}
                                transition={{ duration: 0.2 }}
                            >
                                {errorMessage}
                            </motion.p>
                        )}
                    </AnimatePresence>

                    <form onSubmit={onSubmit} className="space-y-6">
                        <div>
                            <label htmlFor="name" className="block text-gray-700 font-semibold mb-2">
                                Nombre
                            </label>
                            <Input
                                type="text"
                                id="name"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                placeholder="Nombre de la categoría"
                                required
                            />
                        </div>

                        <div>
                            <label htmlFor="description" className="block text-gray-700 font-semibold mb-2">
                                Descripción
                            </label>
                            <textarea
                                id="description"
                                name="description"
                                value={formData.description}
                                onChange={handleChange}
                                placeholder="Descripción de la categoría"
                                rows={4}
                                required
                                className="w-full rounded-card border border-border bg-surface px-4 py-2.5 text-gray-800 shadow-sm transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary"
                            />
                        </div>

                        <Button type="submit" variant="primary" size="lg" className="w-full">
                            {submitLabel}
                        </Button>
                    </form>
                </Card>
            </motion.div>
        </div>
    );
}

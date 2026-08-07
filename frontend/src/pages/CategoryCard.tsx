import React from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { Card } from "../components/ui/Card";

interface CategoryCardProps {
  id: number;
  name: string;
  description: string;
}

const CategoryCard: React.FC<Readonly<CategoryCardProps>> = ({ id, name, description }) => {
  const navigate = useNavigate();

  const handleCardClick = () => {
    navigate(`/dashboard/category/${id}/items`); // Navega a la página de ítems
  };

  return (
    <motion.div whileHover={{ y: -4 }} transition={{ duration: 0.15 }}>
      <Card
        onClick={handleCardClick}
        className="p-4 cursor-pointer border-l-4 border-l-primary hover:shadow-lg transition-shadow"
      >
        <h3 className="text-lg font-semibold text-gray-900">{name}</h3>
        <p className="text-sm text-gray-600 mt-1">{description}</p>
      </Card>
    </motion.div>
  );
};

export default CategoryCard;

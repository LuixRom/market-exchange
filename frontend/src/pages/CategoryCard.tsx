import React from "react";
import { useNavigate } from "react-router-dom";

interface CategoryCardProps {
  id: number;
  name: string;
  description: string;
}

const CategoryCard: React.FC<CategoryCardProps> = ({ id, name, description }) => {
  const navigate = useNavigate();

  const handleCardClick = () => {
    navigate(`/dashboard/category/${id}/items`); // Navega a la página de ítems
  };

  return (
    <div
      onClick={handleCardClick}
      className="bg-purple-600 text-white text-center p-4 rounded-md hover:bg-purple-700 transition cursor-pointer"
    >
      <h3 className="text-lg font-semibold">{name}</h3>
      <p className="text-sm">{description}</p>
    </div>
  );
};

export default CategoryCard;

import AllItems from "../components/AllItems";
import AuthFooter from "../components/AuthFooter";

export default function AllItemsPage() {
  return (
    <div className="min-h-[calc(100vh-80px)] flex flex-col justify-between">
      <div className="flex-grow max-w-container mx-auto px-4 sm:px-6 py-8 w-full">
        <AllItems />
      </div>
      <AuthFooter />
    </div>
  );
}

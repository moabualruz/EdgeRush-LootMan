export function useClassColor() {
  const getClassColor = (className: string) => {
    if (!className) return "bg-gray-500/20 text-gray-500";
    const normalized = className.toUpperCase().replace('_', '');
    return `bg-class-${normalized.toLowerCase()}/20 text-class-${normalized.toLowerCase()}`;
  };

  const getClassTextColor = (className: string) => {
    if (!className) return "text-gray-500";
    const normalized = className.toUpperCase().replace('_', '');
    return `text-class-${normalized.toLowerCase()}`;
  };

  return {
    getClassColor,
    getClassTextColor,
  };
}

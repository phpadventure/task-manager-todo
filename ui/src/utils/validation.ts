export const validateTaskTitle = (title: string): string | null => {
  if (!title || !title.trim()) {
    return 'Task title is required';
  }
  if (title.length > 255) {
    return 'Task title must be less than 255 characters';
  }
  return null;
};

export const validateTaskDescription = (desc: string | undefined): string | null => {
  if (desc && desc.length > 5000) {
    return 'Task description must be less than 5000 characters';
  }
  return null;
};

export const validateListTitle = (title: string): string | null => {
  if (!title || !title.trim()) {
    return 'List title is required';
  }
  if (title.length > 255) {
    return 'List title must be less than 255 characters';
  }
  return null;
};

export const validateDueDate = (date: string | undefined): string | null => {
  if (!date) return null;

  const dueDate = new Date(date);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  dueDate.setHours(0, 0, 0, 0);

  if (dueDate < today) {
    return 'Due date must be today or in the future';
  }

  return null;
};

export const getErrorMessage = (field: string, error: string | null): string => {
  if (!error) return '';

  const errorMap: Record<string, string> = {
    title: error,
    description: error,
    dueDate: error,
  };

  return errorMap[field] || error;
};

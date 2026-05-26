export const formatDate = (date: string | Date): string => {
  if (!date) return '';
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};

export const isOverdue = (dueDate: string | undefined): boolean => {
  if (!dueDate) return false;
  const due = new Date(dueDate);
  const today = new Date();
  due.setHours(0, 0, 0, 0);
  today.setHours(0, 0, 0, 0);
  return due < today;
};

export const isDueSoon = (dueDate: string | undefined): boolean => {
  if (!dueDate) return false;
  const due = new Date(dueDate);
  const today = new Date();
  const inThreeDays = new Date(today);
  inThreeDays.setDate(inThreeDays.getDate() + 3);

  today.setHours(0, 0, 0, 0);
  due.setHours(0, 0, 0, 0);
  inThreeDays.setHours(0, 0, 0, 0);

  return due >= today && due <= inThreeDays;
};

export const isWithinNext7Days = (dueDate: string | undefined): boolean => {
  if (!dueDate) return false;
  const due = new Date(dueDate);
  const today = new Date();
  const in7Days = new Date(today);
  in7Days.setDate(in7Days.getDate() + 7);

  due.setHours(0, 0, 0, 0);
  today.setHours(0, 0, 0, 0);
  in7Days.setHours(0, 0, 0, 0);

  return due >= today && due <= in7Days;
};

export const getDueDateLabel = (dueDate: string | undefined): string => {
  if (!dueDate) return '';

  const due = new Date(dueDate);
  const today = new Date();
  due.setHours(0, 0, 0, 0);
  today.setHours(0, 0, 0, 0);

  const diffTime = due.getTime() - today.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays < 0) return 'Overdue';
  if (diffDays === 0) return 'Today';
  if (diffDays === 1) return 'Tomorrow';
  if (diffDays <= 7) return `In ${diffDays} days`;

  return formatDate(dueDate);
};

export const isToday = (dueDate: string | undefined): boolean => {
  if (!dueDate) return false;
  const due = new Date(dueDate);
  const today = new Date();
  due.setHours(0, 0, 0, 0);
  today.setHours(0, 0, 0, 0);
  return due.getTime() === today.getTime();
};

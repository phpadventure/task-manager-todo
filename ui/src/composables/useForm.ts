import { ref, reactive, computed } from 'vue';

export const useForm = <T extends Record<string, any>>(initialValues: T) => {
  const values = reactive({ ...initialValues });
  const errors = reactive<Record<keyof T, string | null>>({} as any);
  const touched = reactive<Record<keyof T, boolean>>({} as any);
  const isSubmitting = ref(false);

  const isDirty = computed(() =>
    Object.keys(values).some((key) => values[key] !== initialValues[key])
  );

  const isValid = computed(() => Object.values(errors).every((err) => err === null));

  const setFieldValue = (field: keyof T, value: any) => {
    values[field] = value;
  };

  const setFieldError = (field: keyof T, error: string | null) => {
    errors[field] = error;
  };

  const setFieldTouched = (field: keyof T, touched: boolean = true) => {
    touched[field] = touched;
  };

  const setValues = (newValues: Partial<T>) => {
    Object.assign(values, newValues);
  };

  const setErrors = (newErrors: Partial<Record<keyof T, string | null>>) => {
    Object.assign(errors, newErrors);
  };

  const reset = () => {
    Object.assign(values, initialValues);
    Object.keys(errors).forEach((key) => {
      errors[key as keyof T] = null;
    });
    Object.keys(touched).forEach((key) => {
      touched[key as keyof T] = false;
    });
  };

  const handleSubmit = (callback: (values: T) => Promise<void> | void) => {
    return async (e?: Event) => {
      if (e) e.preventDefault();
      isSubmitting.value = true;
      try {
        await callback({ ...values });
      } catch (err) {
        console.error('Form submission error:', err);
      } finally {
        isSubmitting.value = false;
      }
    };
  };

  return {
    values,
    errors,
    touched,
    isDirty,
    isValid,
    isSubmitting,
    setFieldValue,
    setFieldError,
    setFieldTouched,
    setValues,
    setErrors,
    reset,
    handleSubmit,
  };
};

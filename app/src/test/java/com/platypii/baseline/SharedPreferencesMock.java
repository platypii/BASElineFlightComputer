package com.platypii.baseline;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SharedPreferencesMock implements SharedPreferences {

    private final Map<String, Object> preferencesMap = new HashMap<>();
    private final Set<OnSharedPreferenceChangeListener> listeners = new HashSet<>();

    @Override
    public Map<String, ?> getAll() {
        return new HashMap<>(preferencesMap);
    }

    @Nullable
    @Override
    public String getString(final String key, @Nullable final String defValue) {
        final String string = (String) preferencesMap.get(key);
        return string != null ? string : defValue;
    }

    @Nullable
    @Override
    public Set<String> getStringSet(final String key, @Nullable final Set<String> defValues) {
        //noinspection unchecked
        final Set<String> stringSet = (Set<String>) preferencesMap.get(key);
        return stringSet != null ? stringSet : defValues;
    }

    @Override
    public int getInt(final String key, final int defValue) {
        final Integer integer = (Integer) preferencesMap.get(key);
        return integer != null ? integer : defValue;
    }

    @Override
    public long getLong(final String key, final long defValue) {
        final Long longValue = (Long) preferencesMap.get(key);
        return longValue != null ? longValue : defValue;
    }

    @Override
    public float getFloat(final String key, final float defValue) {
        final Float floatValue = (Float) preferencesMap.get(key);
        return floatValue != null ? floatValue : defValue;
    }

    @Override
    public boolean getBoolean(final String key, final boolean defValue) {
        Boolean booleanValue = (Boolean) preferencesMap.get(key);
        return booleanValue != null ? booleanValue : defValue;
    }

    @Override
    public boolean contains(final String key) {
        return preferencesMap.containsKey(key);
    }

    @Override
    public Editor edit() {
        return new EditorImpl();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
        listeners.remove(listener);
    }

    @SuppressWarnings("ConstantConditions")
    public class EditorImpl implements Editor {

        private final Map<String, Object> newValuesMap = new HashMap<>();
        private boolean shouldClear = false;

        @Override
        public Editor putString(final String key, @Nullable final String value) {
            newValuesMap.put(key, value);
            return this;
        }

        @Override
        public Editor putStringSet(final String key, @Nullable final Set<String> values) {
            newValuesMap.put(key, (values != null) ? new HashSet<>(values) : null);
            return this;
        }

        @Override
        public Editor putInt(final String key, final int value) {
            newValuesMap.put(key, value);
            return this;
        }

        @Override
        public Editor putLong(final String key, final long value) {
            newValuesMap.put(key, value);
            return this;
        }

        @Override
        public Editor putFloat(final String key, final float value) {
            newValuesMap.put(key, value);
            return this;
        }

        @Override
        public Editor putBoolean(final String key, final boolean value) {
            newValuesMap.put(key, value);
            return this;
        }

        @Override
        public Editor remove(final String key) {
            // 'this' is marker for remove operation
            newValuesMap.put(key, this);
            return this;
        }

        @Override
        public Editor clear() {
            shouldClear = true;
            return this;
        }

        @Override
        public boolean commit() {
            apply();
            return true;
        }

        @Override
        public void apply() {
            clearIfNeeded();

            final List<String> changedKeys = applyNewValues();
            notifyListeners(changedKeys);
        }

        private void clearIfNeeded() {
            if (shouldClear) {
                shouldClear = false;
                preferencesMap.clear();
            }
        }

        /**
         * @return changed keys list
         */
        private List<String> applyNewValues() {
            final List<String> changedKeys = new ArrayList<>();
            for (Map.Entry<String, Object> entry : newValuesMap.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();

                final boolean isSomethingChanged;
                if (isRemoveValue(value)) {
                    isSomethingChanged = removeIfNeeded(key);
                } else {
                    isSomethingChanged = putValueIfNeeded(key, value);
                }

                if (isSomethingChanged) {
                    changedKeys.add(key);
                }
            }

            newValuesMap.clear();

            return changedKeys;
        }

        private boolean isRemoveValue(@Nullable final Object value) {
            // 'this' is marker for remove operation
            return value == this || value == null;
        }

        /**
         * @return true if element was removed
         */
        private boolean removeIfNeeded(@Nullable final String key) {
            if (preferencesMap.containsKey(key)) {
                preferencesMap.remove(key);
                return true;
            } else {
                return false;
            }
        }

        /**
         * @return true if element was changed
         */
        private boolean putValueIfNeeded(@Nullable final String key, @NonNull final Object value) {
            if (preferencesMap.containsKey(key)) {
                final Object oldValue = preferencesMap.get(key);
                if (value.equals(oldValue)) {
                    return false;
                }
            }

            preferencesMap.put(key, value);
            return true;
        }

        private void notifyListeners(@NonNull final List<String> changedKeys) {
            for (int i = changedKeys.size() - 1; i >= 0; --i) {
                for (OnSharedPreferenceChangeListener listener : listeners) {
                    listener.onSharedPreferenceChanged(SharedPreferencesMock.this, changedKeys.get(i));
                }
            }
            changedKeys.clear();
        }
    }
}

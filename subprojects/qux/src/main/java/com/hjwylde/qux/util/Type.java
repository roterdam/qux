package com.hjwylde.qux.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.hjwylde.qux.util.Types.isSubtype;

import com.hjwylde.common.error.MethodNotImplementedError;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * TODO: Documentation.
 *
 * @author Henry J. Wylde
 * @since 0.1.1
 */
public abstract class Type {

    /**
     * String representation of the {@code any} type.
     */
    public static final String ANY = "A";
    public static final Type.Any TYPE_ANY = new Type.Any();

    public static final Type.List TYPE_LIST_ANY = Type.forList(TYPE_ANY);
    public static final Type.Set TYPE_SET_ANY = Type.forSet(TYPE_ANY);

    /**
     * String representation of the {@code bool} type.
     */
    public static final String BOOL = "B";
    public static final Type.Bool TYPE_BOOL = new Type.Bool();

    /**
     * String representation of the {@code int} type.
     */
    public static final String INT = "Z";
    public static final Type.Int TYPE_INT = new Type.Int();

    /**
     * String representation of the {@code meta} type.
     */
    public static final String META = "M";
    public static final Type.Meta TYPE_META = new Type.Meta();

    /**
     * String representation of the {@code null} type.
     */
    public static final String NULL = "N";
    public static final Type.Null TYPE_NULL = new Type.Null();

    /**
     * String representation of the {@code obj} type.
     */
    public static final String OBJ = "O";
    public static final Type.Obj TYPE_OBJ = new Type.Obj();

    /**
     * String representation of the {@code real} type.
     */
    public static final String REAL = "R";
    public static final Type.Real TYPE_REAL = new Type.Real();

    /**
     * String representation of the {@code str} type.
     */
    public static final String STR = "S";
    public static final Type.Str TYPE_STR = new Type.Str();

    public static final Type TYPE_ITERABLE = Type.forUnion(TYPE_LIST_ANY, TYPE_SET_ANY, TYPE_STR);

    /**
     * String representation of the {@code void} type.
     */
    public static final String VOID = "V";
    public static final Type.Void TYPE_VOID = new Type.Void();

    static final String FUNCTION_START = "(";
    static final String FUNCTION_PARAM_END = ")";

    static final String LIST_START = "[";

    static final String RECORD_START = "C";
    static final String RECORD_END = ";";

    static final String SET_START = "{";

    static final String UNION_START = "U";
    static final String UNION_END = ";";

    /**
     * This class may only be instantiated locally.
     */
    private Type() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        return true;
    }

    public static Type forDescriptor(String desc) {
        return Types.normalise(forDescriptor(desc, true));
    }

    public static Type.Function forFunction(Type returnType, java.util.List<Type> parameterTypes) {
        return Types.normalise(new Type.Function(returnType, parameterTypes));
    }

    public static Type.Function forFunction(Type returnType, Type... parameterTypes) {
        return Types.normalise(new Type.Function(returnType, parameterTypes));
    }

    public static Type.List forList(Type innerType) {
        return new Type.List(Types.normalise(innerType));
    }

    public static Type forRecord(Map<String, Type> fields) {
        return Types.normalise(new Type.Record(fields));
    }

    public static Type.Set forSet(Type innerType) {
        return new Type.Set(Types.normalise(innerType));
    }

    public static Type forUnion(Collection<Type> types) {
        return Types.normalise(new Type.Union(types));
    }

    public static Type forUnion(Type... types) {
        return Types.normalise(new Type.Union(types));
    }

    public abstract String getDescriptor();

    public static Type getFieldType(Type type, String field) {
        if (type instanceof Type.Union) {
            java.util.List<Type> types = new ArrayList<>();
            for (Type bound : ((Type.Union) type).getTypes()) {
                types.add(getFieldType(bound, field));
            }

            return Type.forUnion(types);
        } else if (type instanceof Type.Record) {
            Type.Record record = (Type.Record) type;

            Type expected = Type.forRecord(ImmutableMap.<String, Type>of(field, TYPE_ANY));
            checkArgument(isSubtype(record, expected),
                    "record type '%s' does not contain field '%s'", type, field);

            return ((Type.Record) type).getFields().get(field);
        }

        throw new MethodNotImplementedError(type.getClass().toString());
    }

    public static Type getInnerType(Type type) {
        if (type instanceof Type.Union) {
            java.util.List<Type> inners = new ArrayList<>();
            for (Type bound : ((Type.Union) type).getTypes()) {
                inners.add(getInnerType(bound));
            }

            return Type.forUnion(inners);
        } else if (type instanceof Type.List) {
            return ((Type.List) type).getInnerType();
        } else if (type instanceof Type.Set) {
            return ((Type.Set) type).getInnerType();
        } else if (type instanceof Type.Str) {
            return TYPE_STR;
        }

        throw new MethodNotImplementedError(type.getClass().toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int hashCode();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String toString();

    private static Type forDescriptor(String desc, boolean match) {
        checkArgument(!desc.isEmpty(), "desc cannot be empty");

        switch (desc.substring(0, 1)) {
            case ANY:
                checkArgument(!match || desc.length() == 1, "desc is invalid: %s", desc);
                return TYPE_ANY;
            case BOOL:
                checkArgument(!match || desc.length() == 1, "desc is invalid: %s", desc);
                return TYPE_BOOL;
            case FUNCTION_START:
                java.util.List<Type> parameterTypes = new ArrayList<>();
                int index = 1;
                while (index < desc.length()) {
                    if (desc.startsWith(FUNCTION_PARAM_END, index)) {
                        break;
                    }

                    parameterTypes.add(forDescriptor(desc.substring(index), false));

                    index += parameterTypes.get(parameterTypes.size() - 1).getDescriptor().length();
                }

                checkArgument(desc.startsWith(FUNCTION_PARAM_END, index), "desc is invalid: %s",
                        desc);

                Type returnType = forDescriptor(desc.substring(index + 1), match);

                index += returnType.getDescriptor().length();

                checkArgument(!match || desc.length() == index + 1, "desc is invalid: %s", desc);

                return forFunction(returnType, parameterTypes);
            case INT:
                checkArgument(!match || desc.length() == 1, "desc is invalid: %s", desc);
                return TYPE_INT;
            case LIST_START:
                checkArgument(desc.length() > 1, "desc is invalid: %s", desc);
                return forList(forDescriptor(desc.substring(1), match));
            case NULL:
                checkArgument(!match || desc.length() == 1, "desc is invalid: %s", desc);
                return TYPE_NULL;
            case OBJ:
                checkArgument(!match || desc.length() == 1, "desc is invalid: %s", desc);
                return TYPE_OBJ;
            case REAL:
                checkArgument(!match || desc.length() == 1, "desc is invalid: %s", desc);
                return TYPE_REAL;
            case RECORD_START:
                Map<String, Type> fields = new HashMap<>();
                index = 1;
                while (index < desc.length()) {
                    if (desc.startsWith(RECORD_END, index)) {
                        break;
                    }

                    Type type = forDescriptor(desc.substring(index), false);

                    index += type.getDescriptor().length();

                    String field = desc.substring(index, desc.indexOf(RECORD_END, index));

                    index += field.length() + 1;

                    fields.put(field, type);
                }

                checkArgument(desc.startsWith(RECORD_END, index), "desc is invalid: %s", desc);
                checkArgument(!match || desc.length() == index + 1, "desc is invalid: %s", desc);

                return forRecord(fields);
            case SET_START:
                checkArgument(desc.length() > 1, "desc is invalid: %s", desc);
                return forSet(forDescriptor(desc.substring(1), match));
            case STR:
                checkArgument(!match || desc.length() == 1, "desc is invalid: %s", desc);
                return TYPE_STR;
            case UNION_START:
                java.util.List<Type> types = new ArrayList<>();
                index = 1;
                while (index < desc.length()) {
                    if (desc.startsWith(UNION_END, index)) {
                        break;
                    }

                    types.add(forDescriptor(desc.substring(index), false));

                    index += types.get(types.size() - 1).getDescriptor().length();
                }

                checkArgument(desc.startsWith(UNION_END, index), "desc is invalid: %s", desc);
                checkArgument(!match || desc.length() == index + 1, "desc is invalid: %s", desc);

                return forUnion(types);
            case VOID:
                checkArgument(desc.length() == 1, "desc is invalid: %s", desc);
                return TYPE_VOID;
            default:
                throw new MethodNotImplementedError(desc);
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.1
     */
    public static final class Any extends Type {

        /**
         * This class is a singleton.
         */
        private Any() {}

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescriptor() {
            return ANY;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "any";
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.1
     */
    public static final class Bool extends Type {

        /**
         * This class is a singleton.
         */
        private Bool() {}

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescriptor() {
            return BOOL;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "bool";
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.1
     */
    public static final class Function extends Type {

        private final Type returnType;
        private final ImmutableList<Type> parameterTypes;

        Function(Type returnType, Type... parameterTypes) {
            this(returnType, Arrays.asList(parameterTypes));
        }

        Function(Type returnType, java.util.List<Type> parameterTypes) {
            this.returnType = checkNotNull(returnType, "returnType cannot be null");
            this.parameterTypes = ImmutableList.copyOf(parameterTypes);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(@Nullable Object obj) {
            if (!super.equals(obj)) {
                return false;
            }

            Type.Function function = (Type.Function) obj;

            return returnType.equals(function.returnType) && parameterTypes.equals(
                    function.parameterTypes);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescriptor() {
            StringBuilder sb = new StringBuilder();

            sb.append(FUNCTION_START);
            for (Type parameterType : parameterTypes) {
                sb.append(parameterType.getDescriptor());
            }
            sb.append(FUNCTION_PARAM_END);
            sb.append(returnType.getDescriptor());

            return sb.toString();
        }

        public ImmutableList<Type> getParameterTypes() {
            return parameterTypes;
        }

        public Type getReturnType() {
            return returnType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 2 + 31 * (returnType.hashCode() + 31 * parameterTypes.hashCode());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "(" + Joiner.on(", ").join(parameterTypes) + ") => " + returnType;
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.1
     */
    public static final class Int extends Type {

        /**
         * This class is a singleton.
         */
        private Int() {}

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescriptor() {
            return INT;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 3;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "int";
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.1
     */
    public static final class List extends Type {

        private final Type innerType;

        List(Type innerType) {
            this.innerType = checkNotNull(innerType, "innerType cannot be null");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(@Nullable Object obj) {
            if (!super.equals(obj)) {
                return false;
            }

            return innerType.equals(((Type.List) obj).innerType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescriptor() {
            return LIST_START + innerType.getDescriptor();
        }

        public Type getInnerType() {
            return innerType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 4 + 31 * innerType.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "[" + innerType + "]";
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.2.1
     */
    public static final class Meta extends Type {

        /**
         * This class is a singleton.
         */
        private Meta() {}

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescriptor() {
            return META;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 11;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "meta";
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.1
     */
    public static final class Null extends Type {

        /**
         * This class is a singleton.
         */
        private Null() {}

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescriptor() {
            return NULL;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 5;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "null";
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.2.2
     */
    public static final class Obj extends Type {

        /**
         * This class is a singleton.
         */
        private Obj() {}

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescriptor() {
            return OBJ;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 12;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "obj";
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.1
     */
    public static final class Real extends Type {

        /**
         * This class is a singleton.
         */
        private Real() {}

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescriptor() {
            return REAL;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 6;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "real";
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since TODO: SINCE
     */
    public static final class Record extends Type {

        private final ImmutableMap<String, Type> fields;

        Record(Map<String, Type> fields) {
            checkArgument(!fields.isEmpty(), "fields must contain at least 1 element");

            this.fields = ImmutableMap.copyOf(fields);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(@Nullable Object obj) {
            if (!super.equals(obj)) {
                return false;
            }

            return fields.equals(((Type.Record) obj).fields);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescriptor() {
            StringBuilder sb = new StringBuilder();

            sb.append(RECORD_START);
            for (Map.Entry<String, Type> field : fields.entrySet()) {
                sb.append(field.getValue().getDescriptor());
                sb.append(field.getKey());
                sb.append(RECORD_END);
            }
            sb.append(RECORD_END);

            return sb.toString();
        }

        public ImmutableMap<String, Type> getFields() {
            return fields;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 13 + 31 * fields.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("{");
            for (Map.Entry<String, Type> field : fields.entrySet()) {
                sb.append(field.getValue()).append(" ");
                sb.append(field.getKey());
                sb.append(", ");
            }
            sb.setLength(sb.length() - 2);
            sb.append("}");

            return sb.toString();
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.3
     */
    public static final class Set extends Type {

        private final Type innerType;

        Set(Type innerType) {
            this.innerType = checkNotNull(innerType, "innerType cannot be null");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(@Nullable Object obj) {
            if (!super.equals(obj)) {
                return false;
            }

            return innerType.equals(((Type.Set) obj).innerType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescriptor() {
            return SET_START + innerType.getDescriptor();
        }

        public Type getInnerType() {
            return innerType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 10 + 31 * innerType.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "{" + innerType + "}";
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.1
     */
    public static final class Str extends Type {

        /**
         * This class is a singleton.
         */
        private Str() {}

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescriptor() {
            return STR;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 7;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "str";
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.1
     */
    public static final class Union extends Type {

        private final ImmutableSet<Type> types;

        Union(Type... types) {
            this(Arrays.asList(types));
        }

        Union(Collection<Type> types) {
            checkArgument(!types.isEmpty(), "types must contain at least 1 element");

            this.types = ImmutableSet.copyOf(types);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(@Nullable Object obj) {
            if (!super.equals(obj)) {
                return false;
            }

            return types.equals(((Type.Union) obj).types);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescriptor() {
            StringBuilder sb = new StringBuilder();

            sb.append(UNION_START);
            for (Type type : types) {
                sb.append(type.getDescriptor());
            }
            sb.append(UNION_END);

            return sb.toString();
        }

        public ImmutableSet<Type> getTypes() {
            return types;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 8 + 31 * types.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return Joiner.on("|").join(types);
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.1
     */
    public static final class Void extends Type {

        /**
         * This class is a singleton.
         */
        private Void() {}

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescriptor() {
            return VOID;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 9;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "void";
        }
    }
}

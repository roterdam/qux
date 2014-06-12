package qux.lang;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static qux.lang.Bool.FALSE;
import static qux.lang.Bool.TRUE;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import qux.util.Iterator;

/**
 * TODO: Documentation
 *
 * @author Henry J. Wylde
 * @since 0.1.2
 */
public class Meta extends AbstractObj {

    static final Meta META_ANY = new Any();
    static final Meta META_BOOL = new Bool();
    static final Meta META_INT = new Int();
    static final Meta META_META = new Meta();
    static final Meta META_NULL = new Null();
    static final Meta META_OBJ = new Obj();
    static final Meta META_REAL = new Real();
    static final Meta META_STR = new Str();

    private static final LoadingCache<Meta, Meta> listMetas =
            CacheBuilder.<Meta, Meta>newBuilder().build(new CacheLoader<Meta, Meta>() {
                                                            @Override
                                                            public Meta load(Meta key) {
                                                                return new List(key);
                                                            }
                                                        });
    private static final LoadingCache<Meta, Meta> setMetas =
            CacheBuilder.<Meta, Meta>newBuilder().build(new CacheLoader<Meta, Meta>() {
                                                            @Override
                                                            public Meta load(Meta key) {
                                                                return new Set(key);
                                                            }
                                                        });
    private static final LoadingCache<qux.lang.Set, Meta> unionMetas =
            CacheBuilder.<Meta, Meta>newBuilder().build(new CacheLoader<qux.lang.Set, Meta>() {
                                                            @Override
                                                            public Meta load(qux.lang.Set key) {
                                                                return new Union(key);
                                                            }
                                                        });

    /**
     * This class can only be instantiated locally.
     */
    Meta() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public qux.lang.Int _comp_(AbstractObj obj) {
        if (!(obj instanceof Meta)) {
            return meta()._comp_(obj.meta());
        }

        return _desc_()._comp_(obj._desc_());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public qux.lang.Str _desc_() {
        return qux.lang.Str.valueOf("meta");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Meta _dup_() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public qux.lang.Bool _eq_(AbstractObj obj) {
        if (super._eq_(obj) == FALSE) {
            return FALSE;
        }

        return this == obj ? TRUE : FALSE;
    }

    public static Meta forList(Meta innerType) {
        return listMetas.getUnchecked(normalise(innerType));
    }

    public static Meta forSet(Meta innerType) {
        return setMetas.getUnchecked(normalise(innerType));
    }

    public static Meta forUnion(qux.lang.Set types) {
        return unionMetas.getUnchecked(normalise(types));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Meta meta() {
        return META_META;
    }

    private static qux.lang.Set normalise(qux.lang.Set types) {
        // TODO: Implement normalise(Set)
        throw new InternalError("normalise(Set) not implemented");
    }

    private static Meta normalise(Meta meta) {
        if (meta instanceof Meta.List) {
            return forList(((List) meta).innerType);
        } else if (meta instanceof Meta.Set) {
            return forSet(((Set) meta).innerType);
        } else if (meta instanceof Meta.Union) {
            return forUnion(((Union) meta).types);
        }

        return meta;
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.2
     */
    private static final class Any extends Meta {

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Str _desc_() {
            return qux.lang.Str.valueOf("any");
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.2
     */
    private static final class Bool extends Meta {

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Str _desc_() {
            return qux.lang.Str.valueOf("bool");
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.2
     */
    private static final class Int extends Meta {

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Str _desc_() {
            return qux.lang.Str.valueOf("int");
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.2
     */
    private static final class List extends Meta {

        private final Meta innerType;

        public List(Meta innerType) {
            this.innerType = checkNotNull(innerType, "inner cannot be null");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Str _desc_() {
            return qux.lang.Str.valueOf("[" + innerType + "]");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Bool _eq_(AbstractObj obj) {
            if (super._eq_(obj) == FALSE) {
                return FALSE;
            }

            return innerType._eq_(((List) obj).innerType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Int _hash_() {
            return innerType._hash_();
        }

        public Meta getInnerType() {
            return innerType;
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.2
     */
    private static final class Null extends Meta {

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Str _desc_() {
            return qux.lang.Str.valueOf("null");
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.2.2
     */
    private static final class Obj extends Meta {

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Str _desc_() {
            return qux.lang.Str.valueOf("obj");
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.2
     */
    private static final class Real extends Meta {

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Str _desc_() {
            return qux.lang.Str.valueOf("real");
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.3
     */
    private static final class Set extends Meta {

        private final Meta innerType;

        public Set(Meta innerType) {
            this.innerType = checkNotNull(innerType, "inner cannot be null");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Str _desc_() {
            return qux.lang.Str.valueOf("{" + innerType + "}");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Bool _eq_(AbstractObj obj) {
            if (super._eq_(obj) == FALSE) {
                return FALSE;
            }

            return innerType._eq_(((Set) obj).innerType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Int _hash_() {
            return innerType._hash_();
        }

        public Meta getInnerType() {
            return innerType;
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.2
     */
    private static final class Str extends Meta {

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Str _desc_() {
            return qux.lang.Str.valueOf("str");
        }
    }

    /**
     * TODO: Documentation
     *
     * @author Henry J. Wylde
     * @since 0.1.2
     */
    private static final class Union extends Meta {

        // TODO: Change this to a frozenset when it exists
        private final qux.lang.Set types;

        public Union(qux.lang.Set types) {
            checkArgument(types._len_()._gte_(qux.lang.Int.TWO) == TRUE,
                    "types must have at least 2 elements");

            this.types = types;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Str _desc_() {
            java.util.Iterator<AbstractObj> it = new java.util.Iterator<AbstractObj>() {

                Iterator it = types._iter_();

                @Override
                public boolean hasNext() {
                    return it.hasNext() == TRUE;
                }

                @Override
                public AbstractObj next() {
                    return it.next();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };

            return qux.lang.Str.valueOf(Joiner.on("|").join(it));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Bool _eq_(AbstractObj obj) {
            if (super._eq_(obj) == FALSE) {
                return FALSE;
            }

            return types.equals(((Union) obj).types) ? TRUE : FALSE;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public qux.lang.Int _hash_() {
            return qux.lang.Int.valueOf(types.hashCode());
        }

        public qux.lang.Set getTypes() {
            return types;
        }
    }
}


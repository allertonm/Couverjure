package org.couverjure.core;

import com.sun.jna.*;

import java.util.HashMap;
import java.util.Map;

public class FoundationTypeMapper extends DefaultTypeMapper {
    private static FoundationTypeMapper singleton = new FoundationTypeMapper();

    private Core.Architecture architecture = Core.Architecture.X86_64;
    private FromNativeConverter idFromNativeConverter;
    private ToNativeConverter idToNativeConverter;
    private Map<Class, FromNativeConverter> fromConverters;
    private Map<Class, ToNativeConverter> toConverters;

    public static FoundationTypeMapper getTypeMapper() {
        return singleton;
    }

    public FoundationTypeMapper(Core.Architecture arch) {
        this.architecture = arch;
        this.toConverters = new HashMap<Class, ToNativeConverter>();
        this.fromConverters = new HashMap<Class, FromNativeConverter>();
        switch (this.architecture) {
            case PPC:
            case I386:
                throw new UnsupportedOperationException("32-bit architectures not yet supported.");
            case X86_64:
                toConverters.put(ID.class, new IDToConverter64());
                fromConverters.put(ID.class, new IDFromConverter64());
                break;
        }
    }

    public FoundationTypeMapper() {
        this(Core.Architecture.X86_64);
    }

    class IDToConverter64 implements ToNativeConverter {
        public Object toNative(Object o, ToNativeContext toNativeContext) {
            if (o != null) {
                return ((ID) o).getAddress();
            } else {
                return new Long(0);
            }
        }

        public Class nativeType() {
            return Long.TYPE;
        }
    }

    class IDFromConverter64 implements FromNativeConverter {
        public Object fromNative(Object o, FromNativeContext fromNativeContext) {
            return new ID((Long) o);
        }


        public Class nativeType() {
            return Long.TYPE;
        }
    }

    @Override
    public FromNativeConverter getFromNativeConverter(Class javaType) {
        FromNativeConverter fc = fromConverters.get(javaType);
        if (fc == null) {
            return super.getFromNativeConverter(javaType);
        } else {
            return fc;
        }
    }

    @Override
    public ToNativeConverter getToNativeConverter(Class javaType) {
        ToNativeConverter tc = toConverters.get(javaType);
        if (tc == null) {
            return super.getToNativeConverter(javaType);
        } else {
            return tc;
        }
    }
}

package io.realm.browser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class RealmUtils {
    public static final int DEFAULT_ROW_COUNT = 50;

    public RealmUtils() {
    }

    public static boolean isFieldRealmList(@NonNull Field field) {
        return RealmList.class.isAssignableFrom(field.getType());
    }

    public static boolean isFieldRealmObject(@NonNull Field field) {
        return RealmObject.class.isAssignableFrom(field.getType());
    }

    public static void setNotParamFieldValue(@NonNull RealmObject obj, @NonNull Field field, Object newValue) {
        String methodName = createSetterName(field);

        try {
            Class e = field.getType();
            Method method = obj.getClass().getMethod(methodName, new Class[]{field.getType()});
            method.invoke(obj, new Object[]{newValue});
        } catch (NoSuchMethodException var6) {
            var6.printStackTrace();
        } catch (InvocationTargetException var7) {
            var7.printStackTrace();
        } catch (IllegalAccessException var8) {
            var8.printStackTrace();
        }

    }

    public static Object getNotParamFieldValue(@NonNull RealmObject obj, @NonNull Field field) {
        String methodName = createGetterName(field);

        try {
            Method e = obj.getClass().getMethod(methodName, new Class[0]);
            return e.invoke(obj, new Object[0]);
        } catch (NoSuchMethodException var4) {
            var4.printStackTrace();
        } catch (InvocationTargetException var5) {
            var5.printStackTrace();
        } catch (IllegalAccessException var6) {
            var6.printStackTrace();
        }

        return null;
    }

    public static String getFieldDisplayedName(@NonNull RealmObject obj, @NonNull Field field) {
        if(isFieldRealmList(field)) {
            return getRealmListFieldDisplayingName(obj, field);
        } else if(isFieldRealmObject(field)) {
            RealmObject var9 = getRealmObjectFieldValue(obj, field);
            return var9 == null?"null":getRealmObjectFieldDisplayingName(field);
        } else {
            Object result = getNotParamFieldValue(obj, field);
            if(result == null || field.getType() != byte[].class && field.getType() != Byte[].class) {
                return result == null?"null":result.toString();
            } else {
                byte[] array = (byte[])((byte[])result);
                return getBytesSize(array);
                /*StringBuilder builder = new StringBuilder();
                byte[] arr$ = array;
                int len$ = array.length;
                for(int i$ = 0; i$ < len$; ++i$) {
                    byte b = arr$[i$];
                    builder.append(String.format("0x%02X", new Object[]{Byte.valueOf(b)}));
                    builder.append(" ");
                }

                return builder.toString();*/
            }
        }
    }

    @Nullable
    public static RealmList<RealmObject> getRealmListFieldValue(@NonNull RealmObject obj, @NonNull Field field) {
        String methodName = createGetterName(field);
        RealmList result = null;

        try {
            Method e = obj.getClass().getMethod(methodName, new Class[0]);
            Object resultObj = e.invoke(obj, new Object[0]);
            if(resultObj != null) {
                result = (RealmList)resultObj;
            }
        } catch (NoSuchMethodException var6) {
            var6.printStackTrace();
        } catch (InvocationTargetException var7) {
            var7.printStackTrace();
        } catch (IllegalAccessException var8) {
            var8.printStackTrace();
        }

        return result;
    }

    public static RealmObject getRealmObjectFieldValue(@NonNull RealmObject obj, @NonNull Field field) {
        String methodName = createGetterName(field);
        RealmObject result = null;

        try {
            Method e = obj.getClass().getMethod(methodName, new Class[0]);
            Object resultObj = e.invoke(obj, new Object[0]);
            if(resultObj != null) {
                result = (RealmObject)resultObj;
            }
        } catch (NoSuchMethodException var6) {
            var6.printStackTrace();
        } catch (InvocationTargetException var7) {
            var7.printStackTrace();
        } catch (IllegalAccessException var8) {
            var8.printStackTrace();
        }

        return result;
    }

    public static List<Field> getFields(Class<? extends RealmObject> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        ArrayList result = new ArrayList();
        Field[] arr$ = fields;
        int len$ = fields.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Field f = arr$[i$];
            int mods = f.getModifiers();
            if(!Modifier.isStatic(mods) && !Modifier.isFinal(mods)) {
                result.add(f);
            }
        }

        return result;
    }

    private static String getBytesSize(@NonNull byte[] array) {
        double filesize = array.length/1024D;
        if (filesize >= 1024)
            return String.format(new Locale("pt", "BR"), "%.02f %s", filesize / 1024D , "MB");
        else
            return String.format(new Locale("pt", "BR"), "%.02f %s", filesize, "KB");
    }

    private static String getRealmListFieldDisplayingName(@NonNull RealmObject obj, @NonNull Field field) {
        ParameterizedType pType = (ParameterizedType)field.getGenericType();
        String rawType = pType.getRawType().toString();
        int rawTypeIndex = rawType.lastIndexOf(".");
        if(rawTypeIndex > 0) {
            rawType = rawType.substring(rawTypeIndex + 1);
        }

        String argument = pType.getActualTypeArguments()[0].toString();
        int argumentIndex = argument.lastIndexOf(".");
        if(argumentIndex > 0) {
            argument = argument.substring(argumentIndex + 1);
        }

        int objNumber = getRealmListFieldValue(obj, field).size();
        return String.format("%s<%s> (%d)", new Object[]{rawType, argument, Integer.valueOf(objNumber)});
    }

    private static String getRealmObjectFieldDisplayingName(@NonNull Field field) {
        return field.getType().getSimpleName();
    }

    private static String createGetterName(Field field) {
        String methodName;
        if(field.getType().equals(Boolean.TYPE)) {
            if(field.getName().startsWith("is")) {
                methodName = field.getName();
            } else {
                methodName = "is" + capitalize(field.getName());
            }
        } else {
            methodName = "get" + capitalize(field.getName());
        }

        return methodName;
    }

    private static String createSetterName(Field field) {
        String methodName = "set" + capitalize(field.getName());
        return methodName;
    }

    public static void clearClassData(Realm realm, Class<? extends RealmObject> clazz) {
        realm.beginTransaction();
        realm.where(clazz).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public static RealmList<RealmObject> generateData(Realm realm, Class<? extends RealmObject> clazz, int count) {
        RealmList resultList = new RealmList();
        List fields = getFields(clazz);
        if(fields.size() <= 0) {
            return resultList;
        } else {
            RealmResults existing = realm.where(clazz).findAll();
            int from = existing.size();

            try {
                for(int e = 0; e < count; ++e) {
                    RealmObject obj;
                    if(e < from) {
                        obj = (RealmObject) existing.get(e);
                    } else {
                        obj = (RealmObject)clazz.newInstance();
                        Iterator i$ = fields.iterator();

                        while(i$.hasNext()) {
                            Field field = (Field)i$.next();
                            Object fieldValue = generateFieldValue(realm, field, e);
                            setNotParamFieldValue(obj, field, fieldValue);
                        }
                    }

                    resultList.add(obj);
                }
            } catch (InstantiationException var14) {
                var14.printStackTrace();
            } catch (IllegalAccessException var15) {
                var15.printStackTrace();
            }

            realm.beginTransaction();
            realm.copyToRealm(resultList);
            realm.commitTransaction();
            return resultList;
        }
    }

    private static Object generateFieldValue(Realm realm, Field field, int counter) {
        Class type = field.getType();
        Object value = null;
        if(type == String.class) {
            value = field.getName() + " " + counter;
        } else if(type != Boolean.class && type != Boolean.TYPE) {
            if(type != Short.class && type != Short.TYPE) {
                if(type != Integer.class && type != Integer.TYPE) {
                    if(type != Long.class && type != Long.TYPE) {
                        if(type != Float.class && type != Float.TYPE) {
                            if(type != Double.class && type != Double.TYPE) {
                                if(type == Date.class) {
                                    value = new Date((long)(counter * 1000));
                                } else if(type != Byte.class && type != Byte.TYPE) {
                                    if(type != Byte[].class && type != byte[].class) {
                                        if(RealmObject.class.isAssignableFrom(type)) {
                                            RealmResults pType = realm.where(type).findAll();
                                            int existing = pType.size();
                                            if(existing > 0) {
                                                value = pType.get(0);
                                            } else {
                                                RealmList count = generateData(realm, type, 1);
                                                value = count.size() > 0?count.get(0):null;
                                            }
                                        } else if(RealmList.class.isAssignableFrom(type)) {
                                            ParameterizedType pType1 = (ParameterizedType)field.getGenericType();
                                            Class clazz = (Class)pType1.getActualTypeArguments()[0];
                                            RealmResults existing1 = realm.where(clazz).findAll();
                                            int count1 = existing1.size();
                                            if(count1 < 50) {
                                                value = generateData(realm, clazz, 50);
                                            } else {
                                                RealmList list = new RealmList();
                                                list.addAll(existing1.subList(0, 50));
                                                value = list;
                                            }
                                        } else {
                                            Log.w("GENERATE", "unknown field type");
                                            value = null;
                                        }
                                    } else {
                                        value = Integer.toString(counter).getBytes();
                                    }
                                } else {
                                    value = Byte.valueOf(Integer.valueOf(counter).byteValue());
                                }
                            } else {
                                value = Double.valueOf(Integer.valueOf(counter).doubleValue());
                            }
                        } else {
                            value = Float.valueOf(Integer.valueOf(counter).floatValue());
                        }
                    } else {
                        value = Long.valueOf(Integer.valueOf(counter).longValue());
                    }
                } else {
                    value = Integer.valueOf(counter);
                }
            } else {
                value = Short.valueOf(Integer.valueOf(counter).shortValue());
            }
        } else {
            value = Boolean.valueOf(counter % 2 == 0);
        }

        return value;
    }

    private static String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}

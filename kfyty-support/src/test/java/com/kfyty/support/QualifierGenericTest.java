package com.kfyty.support;

import com.kfyty.support.generic.ActualGeneric;
import com.kfyty.support.generic.QualifierGeneric;
import com.kfyty.support.generic.SimpleGeneric;
import com.kfyty.support.utils.ReflectUtil;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 描述: 泛型测试
 *
 * @author kfyty725
 * @date 2021/6/24 17:58
 * @email kfyty725@hotmail.com
 */
public class QualifierGenericTest {

    public Entity t() {
        return null;
    }

    public Entity[] arrT() {
        return null;
    }

    public List<Entity> listT() {
        return null;
    }

    public Set<Entity> setT() {
        return null;
    }

    public Map<String, Object> map() {
        return null;
    }

    public Map<String, Entity> mapT() {
        return null;
    }

    public List<Map<String, Object>> mapList() {
        return null;
    }

    public <T> List<Map<T[], Map<List<T>[], byte[]>>> nested() {
        return null;
    }

    @Test
    public void test1() throws Exception {
        SimpleGeneric t = SimpleGeneric.from(QualifierGenericTest.class.getMethod("t"));
        SimpleGeneric arrT = SimpleGeneric.from(QualifierGenericTest.class.getMethod("arrT"));
        SimpleGeneric listT = SimpleGeneric.from(QualifierGenericTest.class.getMethod("listT"));
        SimpleGeneric setT = SimpleGeneric.from(QualifierGenericTest.class.getMethod("setT"));
        SimpleGeneric map = SimpleGeneric.from(QualifierGenericTest.class.getMethod("map"));
        SimpleGeneric mapT = SimpleGeneric.from(QualifierGenericTest.class.getMethod("mapT"));
        SimpleGeneric mapList = SimpleGeneric.from(QualifierGenericTest.class.getMethod("mapList"));
        QualifierGeneric nested = QualifierGeneric.from(QualifierGenericTest.class.getMethod("nested"));
        Assert.assertFalse(t.isSimpleParameterizedType());
        Assert.assertTrue(arrT.isSimpleArray());
        Assert.assertTrue(List.class.isAssignableFrom(listT.getSourceType()) && !Map.class.isAssignableFrom(listT.getFirst().get()));
        Assert.assertTrue(Set.class.isAssignableFrom(setT.getSourceType()));
        Assert.assertTrue(map.isMapGeneric() && map.getMapValueType().get().equals(Object.class));
        Assert.assertTrue(mapT.isMapGeneric() && mapT.getMapValueType().get().equals(Entity.class));
        Assert.assertTrue(List.class.isAssignableFrom(mapList.getSourceType()) && mapList.getFirst().get().equals(Map.class));
    }

    @Test
    public void test2() {
        Field t = ReflectUtil.getField(DefaultController.class, "t");
        Field arrT = ReflectUtil.getField(DefaultController.class, "arrT");
        Field service = ReflectUtil.getField(DefaultController.class, "service");
        Field entityClass = ReflectUtil.getField(DefaultController.class, "entityClass");
        ActualGeneric fromT = ActualGeneric.from(DefaultController.class, t);
        ActualGeneric fromArrT = ActualGeneric.from(DefaultController.class, arrT);
        ActualGeneric fromService = ActualGeneric.from(DefaultController.class, service);
        ActualGeneric fromEntityClass = ActualGeneric.from(DefaultController.class, entityClass);
        Assert.assertFalse(fromT.isSimpleParameterizedType());
        Assert.assertEquals(fromT.getSourceType(), Entity.class);
        Assert.assertEquals(fromArrT.getSourceType(), Entity[].class);
        Assert.assertEquals(fromArrT.getFirst().get(), Entity.class);
        Assert.assertEquals(fromService.getFirst().get(), Entity.class);
        Assert.assertEquals(fromService.getSecond().get(), Integer.class);
        Assert.assertEquals(fromEntityClass.getSimpleActualType(), Entity.class);
    }
}

class Entity {}
interface Base<T, K> {}
abstract class BaseImpl<T, K extends Integer> implements Base<T, K> {}
class DefaultBase extends BaseImpl<Entity, Integer> {}

class BaseController<T, K extends Integer> {
    protected T t;
    protected T[] arrT;
    protected Base<T, K> service;
    protected Class<T> entityClass;
}

class DefaultController extends BaseController<Entity, Integer> {}

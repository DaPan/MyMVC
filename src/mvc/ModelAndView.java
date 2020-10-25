package mvc;

import java.util.HashMap;
import java.util.Map;

/**
 * 这个类是为了将两部分信息包装成一个对象
 * 一个是需要存储在request中的数据  Map<String,Object>
 * 一个是响应的视图（路径）名字     String
 */
public class ModelAndView {
    private String pathName;//响应信息  路径的名字
    private HashMap<String, Object> attributeMap = new HashMap<>();

    //以下两个方法给用户使用
    //第一个是为了给用户存储  最终转发路径/视图名字
    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    //第二个是为了给用户存储想存入request.setAttribute()中的数据
    // 每次向map集合内存储一个key-value
    public void addAttribute(String key, Object value) {
        this.attributeMap.put(key, value);
    }

    //============================================
    //以下提供给框架  用来获取信息
    String getPathName() {
        return this.pathName;
    }

    Object getAttribute(String key) {
        return this.attributeMap.get(key);
    }
    HashMap<String,Object> getAttributeMap(){
        return this.attributeMap;
    }
}

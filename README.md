# SpPlugin

### 只需要新建一个Sp的单例对象就可以随意进行sp的存取。

1. 新建一个sp的单例对象

```
  object SpUtil {

    val sp = MyApp.context.getSharedPreferences("nqy", Context.MODE_PRIVATE)

}
```

2. 新建一个你要保存的数据类

```
@Entity(Sp = "com.nqy.spdemo.SpUtil.sp")
data class User(
    @Column(defValue = "小明")
    val name: String,
    @Column(defValue = "12")
    val age: Int
)
```

3. 点击Studio的Build -> Make Project，会自动生成数据的操作类

4. 使用

```
tv.text = "name:${UserSP.name},age:${UserSP.age}"
```

赋值就和普通的对象赋值一样
```
UserSP.name = "nqy"
UserSP.age = 16
```

需要在项目的app模块下的build.gradle文件中添加


```
apply plugin: 'kotlin-kapt'

...

implementation project(path: ':nqy_anno')
kapt project(path: ':sp_compiler')

```


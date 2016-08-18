/**@author:idevcod@163.com
 * @date:2016年8月16日下午11:32:11
 * @description:<TODO>
 */
package model;

public class Record
{
    private String str;

    public Record()
    {
    }

    @Override
    public String toString()
    {
        return "Record [str=" + str + "]";
    }

    public Record(String str)
    {
        this.str = str;
    }

    public String getStr()
    {
        return str;
    }

    public void setStr(String str)
    {
        this.str = str;
    }
}

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
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((str == null) ? 0 : str.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Record other = (Record) obj;
        if (str == null)
        {
            if (other.str != null)
                return false;
        } else if (!str.equals(other.str))
            return false;
        return true;
    }
}

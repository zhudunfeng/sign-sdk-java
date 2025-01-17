package com.github.aiosign.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
 * @author Zhu Dunfeng
 * @date 2023/5/23
 */
public class SealUtils {
    /**
     * 转换印章实际大小到像素值
     * 换算规则:  val*(720/254)
     * @param val 印章实际大小
     * @return {@code Double} 印章实际大小对应的像素大小
     */
    public static Double transitionSizeToPixel(Double val){
        BigDecimal bigDecimal720 = new BigDecimal("720");
        BigDecimal bigDecimal254 = new BigDecimal("254");
        BigDecimal rate = bigDecimal720.divide(bigDecimal254,20, RoundingMode.HALF_UP);
        BigDecimal bigDecimalVal = new BigDecimal(val+"");
        bigDecimalVal= bigDecimalVal.multiply(rate);
        return bigDecimalVal.doubleValue();
    }

    /**
     * 转换像素值到印章实际大小
     * 换算规则: val/(720/254)
     * @param val 印章像素值
     * @return {@code Double} 印章像素大小对应的实际大小
     */
    public static Double transitionPixelToSize(Double val){
        BigDecimal bigDecimal720 = new BigDecimal("720");
        BigDecimal bigDecimal254 = new BigDecimal("254");
        BigDecimal rate = bigDecimal720.divide(bigDecimal254,20, RoundingMode.HALF_UP);
        BigDecimal bigDecimalVal = new BigDecimal(val+"");
        bigDecimalVal= bigDecimalVal.divide(rate,RoundingMode.HALF_UP);
        return bigDecimalVal.doubleValue();
    }

    /**
     * 判断电子印章与实物印章是否一致
     * @param formType 印章形态 5,6为一致
     * @return 是否一致
     */
    public static Boolean checkSealFormType(String formType){
        List<String> legalFormTypeList = Arrays.asList("5", "6");
        return legalFormTypeList.contains(formType);
    }

}

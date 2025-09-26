package com.zhousheng.common.exception;

public class StockNotEnoughException extends Exception{
    public StockNotEnoughException(){
        super("发生错误，商品库存不足");
    }
}

package com.gicbd.dialappbg.interfaces;

public interface StoreDataView {
    public void onStoreSuccess(String storeData);

    public void onStoreError(String error);

    public void onLogout(int code);

    public void singleSuccess(String res);

    public void singleError(String error);

    public void singleSuccessCode(int code);
    
}

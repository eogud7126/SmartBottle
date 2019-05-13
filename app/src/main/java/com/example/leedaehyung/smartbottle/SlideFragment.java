package com.example.leedaehyung.smartbottle;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Lee DaeHyung on 2019-03-22.
 */

public class SlideFragment extends Fragment {
    private int layoutResId=0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Slide slide = new Slide();
        if(getArguments()!=null && getArguments().containsKey(slide.ARG_LAYOUT_RES_ID)){
            layoutResId=getArguments().getInt(slide.ARG_LAYOUT_RES_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layoutResId,container,false);
    }
    class Slide{
        private String ARG_LAYOUT_RES_ID="layoutResId";

        public SlideFragment newInstance(int layoutResId) {

            SlideFragment sampleSlide=new SlideFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_RES_ID,layoutResId);
            sampleSlide.setArguments(args);
            return sampleSlide;
        }
    }
}

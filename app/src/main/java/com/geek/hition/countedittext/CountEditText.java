package com.geek.hition.countedittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class CountEditText extends RelativeLayout{

    //类型1(单数类型)：TextView显示总字数，然后根据输入递减.例：100，99，98
    //类型2(百分比类型)：TextView显示总字数和当前输入的字数，例：0/100，1/100，2/100
    public static final int MODE_REMAINING = 1;//类型1(单数类型)
    public static final int MODE_PERCENTAGE = 2;//类型2(百分比类型)
    private EditText etContent;//文本框
    private TextView tvNum;//字数显示TextView
    private int mMode ;//类型
    private int maxLength = 100;//最大字符
    private String mHintText;
    private float editTextSize = 15;
    private float countTextSize = 10;
    private int editTextColor = Color.BLACK ;


    public CountEditText(Context context) {
        this(context,null);
    }

    public CountEditText(Context context, AttributeSet attrs) {
        this(context,attrs,-1);
    }

    public CountEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.CountEditText);
        maxLength = array.getInt(R.styleable.CountEditText_ce_maxLength,maxLength);
        if(array.hasValue(R.styleable.CountEditText_ce_countMode)){
            mMode = array.getInt(R.styleable.CountEditText_ce_countMode,MODE_PERCENTAGE);
        }
        mHintText = array.getString(R.styleable.CountEditText_ce_hint);
        editTextSize = array.getDimension(R.styleable.CountEditText_ce_edit_text_size,editTextSize);
        editTextColor = array.getColor(R.styleable.CountEditText_ce_edit_text_color,editTextColor);
        countTextSize = array.getDimension(R.styleable.CountEditText_ce_count_text_size,countTextSize);
        array.recycle();

        LayoutInflater.from(context).inflate(R.layout.anfq_num_edittext, this, true);
        etContent = (EditText) findViewById(R.id.etContent);
        tvNum = (TextView) findViewById(R.id.tvNum);

        if(mMode == MODE_REMAINING){//类型1
            tvNum.setText(String.valueOf(maxLength));
        }else if(mMode == MODE_PERCENTAGE){//类型2
            tvNum.setText(0+"/"+maxLength);
        }
        etContent.setHint(mHintText);
        etContent.setTextSize(editTextSize);
        etContent.setTextColor(editTextColor);
        InputFilter[] filters = {new InputFilter.LengthFilter(maxLength)};
        etContent.setFilters(filters);
        etContent.addTextChangedListener(mTextWatcher);
        tvNum.setTextSize(countTextSize);
    }

    // 输入监听
    private TextWatcher mTextWatcher = new TextWatcher() {
        private int editStart;
        private int editEnd;

        public void afterTextChanged(Editable s) {
            editStart = etContent.getSelectionStart();
            editEnd = etContent.getSelectionEnd();
            // 先去掉监听器，否则会出现栈溢出
            etContent.removeTextChangedListener(mTextWatcher);
            // 注意这里只能每次都对整个EditText的内容求长度，不能对删除的单个字符求长度
            // 因为是中英文混合，单个字符而言，calculateLength函数都会返回1
            while (calculateLength(s.toString()) > maxLength) { // 当输入字符个数超过限制的大小时，进行截断操作
                s.delete(editStart - 1, editEnd);
                editStart--;
                editEnd--;
            }
            // 恢复监听器
            etContent.addTextChangedListener(mTextWatcher);
            setLeftCount();
        }

        public void beforeTextChanged(CharSequence s, int start, int count,int after) {}

        public void onTextChanged(CharSequence s, int start, int before,int count) {}
    };


    /** 刷新剩余输入字数 */
    private void setLeftCount() {
        //  获取用户输入内容字数
        long inputCount = calculateLength(etContent.getText().toString());
        if(mMode == MODE_REMAINING){//类型1
            tvNum.setText(String.valueOf(maxLength - inputCount));
        }else if(mMode == MODE_PERCENTAGE){//类型2
            tvNum.setText(maxLength-(maxLength - inputCount)+"/"+maxLength);
        }
    }

    /**
     * 计算分享内容的字数，一个汉字=两个英文字母，一个中文标点=两个英文标点
     * 注意：该函数的不适用于对单个字符进行计算，因为单个字符四舍五入后都是1
     * @param cs
     * @return
     */
    public static long calculateLength(CharSequence cs) {
        double len = 0;
        for (int i = 0; i < cs.length(); i++) {
            int tmp = (int) cs.charAt(i);
            if (tmp > 0 && tmp < 127) {
                len += 1;
            } else {
                len++;
            }
        }
        return Math.round(len);
    }

}

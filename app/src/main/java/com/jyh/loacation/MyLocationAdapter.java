package com.jyh.loacation;


import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;

public class MyLocationAdapter extends BaseAdapter {
	private Context mContext;
	private List<PoiItem> mList;
	private int mWhere=0;
	public MyLocationAdapter(Context context, List<PoiItem> list) {
		mContext = context;
		mList = list;
	}
	public void setData(List<PoiItem> list) {
		mList = list;
		notifyDataSetChanged();
	}

	public void showSelect(int where) {
		mWhere=where;
		Log.e("TAG", "dianjile"+where);
		notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		if (mList == null)
			return 0;
		return mList.size();
	}
	@Override
	public Object getItem(int position) {
		if (mList == null)
			return null;
		return mList.get(position);
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHodler viewhodler = null;
		if (convertView == null) { 
			convertView =View.inflate(mContext, R.layout.location_item, null);
			viewhodler = new ViewHodler();
			viewhodler.tvName = (TextView) convertView
					.findViewById(R.id.tvName);
			viewhodler.tvAddstr = (TextView) convertView
					.findViewById(R.id.tvAddstr);
			viewhodler.img=(ImageView) convertView.findViewById(R.id.img);
			convertView.setTag(viewhodler);
		} else {
			viewhodler = (ViewHodler) convertView.getTag();
		}
		PoiItem poiItem=	mList.get(position);
		if (poiItem!=null){
			String title=mList.get(position).getTitle();
			String addstr=mList.get(position).getSnippet();
			viewhodler.tvName.setText(title);
			viewhodler.tvAddstr.setText(addstr);
		}
		if (mWhere==position) {
			viewhodler.img.setVisibility(View.VISIBLE);
			viewhodler.tvName.setTextColor(Color.BLUE);//mContext.getResources().getColorStateList(R.color.blue)
			viewhodler.tvAddstr.setTextColor(Color.BLUE);//mContext.getResources().getColorStateList(R.color.blue)
		}else
		{
			viewhodler.img.setVisibility(View.INVISIBLE);
			viewhodler.tvName.setTextColor(Color.BLACK);//mContext.getResources().getColorStateList(R.color.black)
			viewhodler.tvAddstr.setTextColor(Color.GRAY);//mContext.getResources().getColorStateList(R.color.gray)
		}
		return convertView;
	}
	class ViewHodler {
		TextView tvName, tvAddstr;
		ImageView img;
	}
}

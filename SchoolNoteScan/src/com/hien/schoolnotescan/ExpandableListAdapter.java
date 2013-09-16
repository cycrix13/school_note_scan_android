package com.hien.schoolnotescan;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hien.schoolnotescan.Document.Tag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
 
    private MainActivity mContext;
    private Map<String, List<String>> menuCollections = new LinkedHashMap<String, List<String>>();
    private List<String> groupMenuList = new ArrayList<String>();
 
    ///////////////////////////////////////////////////////////////////////////
    // Public methods
    ///////////////////////////////////////////////////////////////////////////
    
    public ExpandableListAdapter(MainActivity context, ExpandableListView explst) {
    	
        mContext = context;
        
        initMenu();
        
        explst.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				
				switch (groupPosition) {
				case 0:
					switch (childPosition) {
					case 0:
						mContext.onDocumentsClick();
						break;
					case 1:
						mContext.onTutorialClick();
						break;
					case 2:
						mContext.onWifiSharingClick();
						break;
					}
					break;
				case 1:
					mContext.onTagsClick(childPosition, (String) getChild(1, childPosition));
					break;
				case 2:
					switch (childPosition) {
					case 0:
						mContext.onAboutClick();
						break;
					case 1:
						mContext.onHelpClick();
						break;
					case 2:
						mContext.onRestorePurchaseClick();
						break;
					case 3:
						mContext.onUnlockPremium();
						break;
					}
					break;
				}
				
				return false;
			}
		});
    }
    
    public void initMenu() {

    	addGroupMenu(""); {
    		addChildMenu("Documents");
    		addChildMenu("Tutorial");
    		addChildMenu("Wifi Sharing"); }
    	addGroupMenu("Tags");
    	addGroupMenu("Settings"); {
    		addChildMenu("About");
    		addChildMenu("Help");
    		addChildMenu("Restore purchase");
    		addChildMenu("Unlock Premium"); }
    }
    
    private void addGroupMenu(String menu) {
    	
    	groupMenuList.add(menu);
    	menuCollections.put(menu, new ArrayList<String>());
    }
    
    private void addChildMenu(String menu) {
    	
    	menuCollections.get(groupMenuList.get(groupMenuList.size() - 1)).add(menu);
    }
    
    public void SetTagList(List<Tag> tagList) {
    	
    	List<String> menuTagList = menuCollections.get(groupMenuList.get(1));
    	menuTagList.clear();
    	for (Tag t : tagList)
    		menuTagList.add(t.tag);
    	notifyDataSetChanged();
    }
 
    public Object getChild(int groupPosition, int childPosition) {
    	
        return menuCollections.get(groupMenuList.get(groupPosition)).get(childPosition);
    }
 
    public long getChildId(int groupPosition, int childPosition) {
    	
        return childPosition;
    }
 
    public View getChildView(final int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
    	
        String menu = (String) getChild(groupPosition, childPosition);
        LayoutInflater inflater = mContext.getLayoutInflater();
 
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.child_menu_item, null);
        }
        
        ((TextView) convertView.findViewById(R.id.txtChildMenuName)).setText(menu); 
 
        return convertView;
    }
 
    public int getChildrenCount(int groupPosition) {
    	
        return menuCollections.get(groupMenuList.get(groupPosition)).size();
    }
 
    public Object getGroup(int groupPosition) {
    	
        return groupMenuList.get(groupPosition);
    }
 
    public int getGroupCount() {
    	
        return groupMenuList.size();
    }
 
    public long getGroupId(int groupPosition) {
    	
        return groupPosition;
    }
 
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
    	
        String menuName = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.group_menu_item,
                    null);
        }
        
        View layoutChild = convertView.findViewById(R.id.layoutChildMenu);
        if (menuName.length() == 0) {
        	layoutChild.setVisibility(View.GONE);
        } else {
        	layoutChild.setVisibility(View.VISIBLE);
        }
        
        ((TextView) convertView.findViewById(R.id.txtGroupMenuName)).setText(menuName);
        
        return convertView;
    }
 
    public boolean hasStableIds() {
    	
        return true;
    }
 
    public boolean isChildSelectable(int groupPosition, int childPosition) {
    	
        return true;
    }
}
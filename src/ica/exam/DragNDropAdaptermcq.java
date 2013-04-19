package ica.exam;


import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public final class DragNDropAdaptermcq extends BaseAdapter implements RemoveListener, DropListener{

	private int[] mIds;
    private int[] mLayouts;
    private LayoutInflater mInflater;
    private static ArrayList<DataObject_mcq> mContent;

    public DragNDropAdaptermcq(Context context, ArrayList<DataObject_mcq> content) {
        init(context, new int[]{android.R.layout.simple_list_item_1}, new int[]{android.R.id.text1}, content);
    }
    
    public DragNDropAdaptermcq(Context context, int[] itemLayouts, int[] itemIDs, ArrayList<DataObject_mcq> content) {
    	init(context, itemLayouts, itemIDs, content);
    }

    private void init(Context context, int[] layouts, int[] ids, ArrayList<DataObject_mcq> content) {
    	mInflater = LayoutInflater.from(context);
    	mIds = ids;
    	mLayouts = layouts;
    	mContent = content;
    }
    
    public int getCount() {
        return mContent.size();
    }

    public DataObject_mcq getItem(int position) {
        return mContent.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(mLayouts[0], null);
            holder = new ViewHolder();
            holder.tvQuestion = (TextView) convertView.findViewById(mIds[0]);
            holder.tvAnswer = (TextView) convertView.findViewById(mIds[1]);
            
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            convertView.setTag(holder);
        }

        holder.tvQuestion.setText(mContent.get(position).getQuestion());
        holder.tvAnswer.setText(mContent.get(position).getAnswer());
        
        return convertView;
    }

    static class ViewHolder {
        TextView tvQuestion;
        TextView tvAnswer;
    }

	public void onRemove(int which) {
		if (which < 0 || which > mContent.size()) return;		
		mContent.remove(which);
	}

	public void onDrop(int from, int to) {
		DataObject_mcq tempfrom = mContent.get(from);
		DataObject_mcq tempto = mContent.get(to);

		mContent.remove(from);
		DataObject_mcq datafrom = new DataObject_mcq();
		datafrom.setQuestion(tempfrom.getQuestion());
		datafrom.setAnswer(tempto.getAnswer());
		mContent.add(from,datafrom);
		
		mContent.remove(to);
		DataObject_mcq datato = new DataObject_mcq();
		datato.setQuestion(tempto.getQuestion());
		datato.setAnswer(tempfrom.getAnswer());
		mContent.add(to,datato);
	}
}
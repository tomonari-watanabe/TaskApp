package jp.techacademy.tomonari.watanabe.taskapp;

        import android.app.AlarmManager;
        import android.app.PendingIntent;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.os.Bundle;
        import android.support.design.widget.FloatingActionButton;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ListView;

        import java.util.ArrayList;
        import java.util.Date;

        import io.realm.Realm;
        import io.realm.RealmChangeListener;
        import io.realm.RealmResults;
        import io.realm.Sort;


public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_TASK = "jp.techacademy.tomonari.watanabe.taskapp.TASK";

    private Realm mRealm;
    private RealmResults<Task> mTaskRealmResults;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange() {
            reloadListView();
        }
    };

    private ListView mListView;
    private TaskAdapter mTaskAdapter;

    EditText msort;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msort = (EditText) findViewById(R.id.Category);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });


        Button fab2 = (Button) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRealm = Realm.getDefaultInstance();
                mTaskRealmResults = mRealm.where(Task.class).contains("category", msort.getText().toString()).findAll();
                mTaskRealmResults.sort("date", Sort.DESCENDING);
                mRealm.addChangeListener(mRealmListener);
                reloadListView();
            }
        });



        mRealm = Realm.getDefaultInstance();
        mTaskRealmResults = mRealm.where(Task.class).findAll();
        mTaskRealmResults.sort("date", Sort.DESCENDING);
        mRealm.addChangeListener(mRealmListener);

        //ListViewの設定

        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);



        // ListViewをタップした時の処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //入力・編集させる画面に遷移
                Task task = (Task) parent.getAdapter().getItem(position);

                Intent intent = new Intent(MainActivity.this,InputActivity.class);
                intent.putExtra(EXTRA_TASK, task);


                startActivity(intent);

            }
        });

        //ListViewを長押ししたとき
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // タスクを削除する

                final Task task = (Task) parent.getAdapter().getItem(position);

                //ダイログを表示
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage(task.getTitle() +"を削除しますか？");
                builder.setPositiveButton("OK",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dailog, int which){

                        RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();

                        mRealm.beginTransaction();
                        results.clear();
                        mRealm.commitTransaction();

                        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                                MainActivity.this,
                                task.getId(),
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT

                        );

                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(resultPendingIntent);


                        reloadListView();
                    }



                });

                builder.setNegativeButton("CANCEL", null);

                AlertDialog dialog = builder.create();
                dialog.show();


                return true;
            }
        });

        if (mTaskRealmResults.size() == 0) {
            //アプリ起動時にタスクが0のときよう
            addTaskForTest();
        }



        reloadListView();
    }



    private void reloadListView() {

        ArrayList<Task> taskArrayList = new ArrayList<>();

        for (int i = 0; i < mTaskRealmResults.size() ; i++){

            Task task = new Task();


            task.setId(mTaskRealmResults.get(i).getId());
            task.setTitle(mTaskRealmResults.get(i).getTitle());
            task.setContents(mTaskRealmResults.get(i).getContents());
            task.setCategory(mTaskRealmResults.get(i).getCategory());
            task.setDate(mTaskRealmResults.get(i).getDate());

            taskArrayList.add(task);

        }


        mTaskAdapter.setTaskArrayList(taskArrayList);
        mListView.setAdapter(mTaskAdapter);
        mTaskAdapter.notifyDataSetChanged();


    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        mRealm.close();
    }

    private void addTaskForTest(){

        Task task = new Task();
        task.setTitle("例");
        task.setCategory("カテゴリ");
        task.setContents("内容");
        task.setDate(new Date());
        task.setId(0);
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(task);
        mRealm.commitTransaction();

    }


}

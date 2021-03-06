package kr.ac.mju.cd2020shwagwan.ui.AdditionalInformation.lowest.search;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import kr.ac.mju.cd2020shwagwan.ui.AdditionalInformation.lowest.apiInterface.LowestApiInterface;
import kr.ac.mju.cd2020shwagwan.ui.AdditionalInformation.lowest.listener.EndlessRecyclerViewScrollListener;
import kr.ac.mju.cd2020shwagwan.ui.AdditionalInformation.lowest.repository.ResponseInfo;
import kr.ac.mju.cd2020shwagwan.ui.AdditionalInformation.lowest.repository.ResponseItem;
import kr.ac.mju.cd2020shwagwan.ui.AdditionalInformation.lowest.util.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import static kr.ac.mju.cd2020shwagwan.ui.AdditionalInformation.lowest.util.Constants.LOWEST_DISPLAY_SIZE;

public class SearchPresenter implements SearchContract.Presenter {

    private static final String TAG = SearchPresenter.class.getName();
    private SearchContract.View spSearchView;
    private Retrofit spRetrofit;
    private LowestApiInterface spLowestApiInterface;
    private Call<ResponseInfo> spCallLowInfoList;
    private int spPageNo;
    private String spStrName, spStrKind;
    private ArrayList<ResponseItem> spResultArrList = new ArrayList();
    int addCount = 0;
    int overThousand = 0;

    public SearchPresenter(@NonNull SearchContract.View searchView, String baseUrl, String saName, String saKind) {
        spRetrofit = RetrofitClient.getClient(baseUrl);
        spLowestApiInterface = spRetrofit.create(LowestApiInterface.class);
        spSearchView = searchView;
        spSearchView.setPresenter(this);
        spStrName = saName;
        spStrKind = saKind;
    }

    @Override
    public void start() {
    }

    @Override
    public void startSearch(String title) {
        EndlessRecyclerViewScrollListener.ervslCurrentPage = 0;
        if (title.isEmpty()) {
            spSearchView.showEmptyField();
        } else {
            spPageNo = 1;
            getLowCos(title, 1, "asc");
        }
    }



    @Override
    public void getLowCos(String title, int startPosition, String sortWay) {
        if ((spPageNo != -1 || startPosition == 1) && startPosition < 1001) {
            spPageNo = startPosition;
            spCallLowInfoList = spLowestApiInterface.getLowestList(title, LOWEST_DISPLAY_SIZE, startPosition, sortWay);
            spCallLowInfoList.enqueue(spRetrofitCallback);
        }
        if (spStrKind.equals("기타")){
            overThousand++;
        }
        if (overThousand == 0 && 1000 < startPosition){
            startSearch(spStrName+spStrKind);
            overThousand++;
        }
    }

    private Callback<ResponseInfo> spRetrofitCallback = new Callback<ResponseInfo>() {

        @Override
        public void onResponse(Call<ResponseInfo> call, Response<ResponseInfo> response) {
            ResponseInfo spResult = response.body();
            if (spResult != null) {
                if (spResult.getItems() == null) {
                    spPageNo = -1;
                    return;
                }
               /* if (spResult.getItems().size() == 0) {
                    spSearchView.showNotFindItem();
                    return;
                }*/ else {
                    for (int i = 0; i < spResult.getDisplay(); i++) {
                        if (spResult.getItems().get(i).getCategory1().equals("화장품/미용")) {
                            spResultArrList.add(spResult.getItems().get(i));
                        }
                    }
                    if (spResultArrList.size() == 0) {
                        EndlessRecyclerViewScrollListener.ervslCurrentPage++;
                        getLowCos(spStrName, EndlessRecyclerViewScrollListener.ervslCurrentPage * LOWEST_DISPLAY_SIZE + 1, "asc");
                    } else {
                        if (addCount == 0) {
                            spSearchView.showNewLowCos(spResultArrList);
                            overThousand++;
                            addCount++;
                        } else {
                            spSearchView.showMoreLowCos(spResultArrList);
                        }
                        spResultArrList.clear();
                        return;
                    }
                }
                if (spResult.getItems().size() < LOWEST_DISPLAY_SIZE) {
                    spPageNo = -1;
                    return;
                }
            }
        }
        @Override
        public void onFailure(Call<ResponseInfo> call, Throwable t) {
            t.printStackTrace();
        }

    };
}
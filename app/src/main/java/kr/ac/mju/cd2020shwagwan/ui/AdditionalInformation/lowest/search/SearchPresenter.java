package kr.ac.mju.cd2020shwagwan.ui.AdditionalInformation.lowest.search;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import kr.ac.mju.cd2020shwagwan.ui.AdditionalInformation.lowest.apiInterface.LowestApiInterface;
import kr.ac.mju.cd2020shwagwan.ui.AdditionalInformation.lowest.repository.ResponseInfo;
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

    public SearchPresenter(@NonNull SearchContract.View searchView, String baseUrl) {
        spRetrofit = RetrofitClient.getClient(baseUrl);
        spLowestApiInterface = spRetrofit.create(LowestApiInterface.class);
        spSearchView = searchView;
        spSearchView.setPresenter(this);
    }

    @Override
    public void start() {}

    @Override
    public void startSearch(String title) {
        if (title.isEmpty()) {
            spSearchView.showEmptyField();
        } else {
            spPageNo = 1;
            getLowCos(title, 1);
        }
    }

    @Override
    public void getLowCos(String title, int startPosition) {
        if ((spPageNo != -1 || startPosition == 1) && startPosition < 1001) {
            spPageNo = startPosition;
            spCallLowInfoList = spLowestApiInterface.getLowestList(title, LOWEST_DISPLAY_SIZE, startPosition);
            spCallLowInfoList.enqueue(spRetrofitCallback);
        }
    }

    private Callback<ResponseInfo> spRetrofitCallback = new Callback<ResponseInfo>() {

        @Override
        public void onResponse(Call<ResponseInfo> call, Response<ResponseInfo> response) {
            ResponseInfo spResult = response.body();
            if (spResult.getItems() == null) {
                spPageNo = -1;
                return;
            }
            if (spResult.getItems().size() == 0) {
                spSearchView.showNotFindItem();
            } else if (spPageNo <= LOWEST_DISPLAY_SIZE) {
                spSearchView.showNewLowCos(new ArrayList<>(spResult.getItems()));
            } else {
                spSearchView.showMoreLowCos(new ArrayList<>(spResult.getItems()));
            }
            if (spResult.getItems().size() < LOWEST_DISPLAY_SIZE) {
                spPageNo = -1;
            }
        }

        @Override
        public void onFailure(Call<ResponseInfo> call, Throwable t) {
            t.printStackTrace();
        }

    };

}
package com.ivan.mymovies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ivan.mymovies.data.MainViewModel;
import com.ivan.mymovies.data.Movie;
import com.ivan.mymovies.utils.JSONUtils;
import com.ivan.mymovies.utils.NetworkUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Switch switshSort;
    private RecyclerView recyclerViewPoster;
    private MovieAdapter movieAdapter;
    private TextView textViewTopRated;
    private TextView textViewPopularity;

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        switshSort = findViewById(R.id.switchSort);
        textViewPopularity = findViewById(R.id.textViewPopularity);
        textViewTopRated = findViewById(R.id.textViewTopRated);
        recyclerViewPoster = findViewById(R.id.recyclerViewPosters);
        recyclerViewPoster.setLayoutManager(new GridLayoutManager(this,2));
        movieAdapter = new MovieAdapter();
        recyclerViewPoster.setAdapter(movieAdapter);
        switshSort.setChecked(true);
        switshSort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setMethodOfSort(isChecked);
            }
        });
        switshSort.setChecked(false);
        movieAdapter.setOnPosterClickListener(new MovieAdapter.OnPosterClickListener() {
            @Override
            public void onPosterClick(int position) {
                Toast.makeText(MainActivity.this,"Clicked: " + position, Toast.LENGTH_SHORT).show();
            }
        });
        movieAdapter.setOnReachEndListener(new MovieAdapter.OnReachEndListener() {
            @Override
            public void onReachEnd() {
                Toast.makeText(MainActivity.this, "Конец списка", Toast.LENGTH_SHORT).show();
            }
        });
        LiveData<List<Movie>> moviesFromLiveData = viewModel.getMovies();
        moviesFromLiveData.observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                movieAdapter.setMovies(movies);
            }
        });
    }

    public void onClickSetPopularity(View view) {
        setMethodOfSort(false);
        switshSort.setChecked(false);
    }

    public void onClickSetTopRated(View view) {
        setMethodOfSort(true);
        switshSort.setChecked(true);
    }

    private void setMethodOfSort(boolean isTopRated) {
        int methodOfSort;
        if  (isTopRated) {
            textViewTopRated.setTextColor(getResources().getColor(R.color.colorAccent));
            textViewPopularity.setTextColor(getResources().getColor(R.color.white_color));
            methodOfSort = NetworkUtils.TOP_RATED;
        } else {
            textViewPopularity.setTextColor(getResources().getColor(R.color.colorAccent));
            textViewTopRated.setTextColor(getResources().getColor(R.color.white_color));
            methodOfSort = NetworkUtils.POPULARITY;
        }
        downloadData(methodOfSort,1);
    }

    private void downloadData(int methodOfSort, int page) {
        JSONObject jsonObject = NetworkUtils.getJSONFromNetwork(methodOfSort,1);
        ArrayList<Movie> movies = JSONUtils.getMoviesFromJSON(jsonObject);
        if (movies != null && !movies.isEmpty()) {
            viewModel.deleteAllMovie();
            for (Movie movie : movies) {
                viewModel.insertMovies(movie);
            }
        }
        movieAdapter.setMovies(movies);
    }
}
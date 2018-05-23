
package com.arreev.android.followers

import android.os.*
import javax.inject.*
import android.view.*

import gubo.slipwire.*
import com.arreev.android.*
import com.arreev.android.databinding.FollowersfragmentBinding

/**
 * Created by jeffschulz on 5/17/18.
 */
class FollowersFragment : android.support.v4.app.Fragment()
{
    @Inject lateinit var personsPresenter: PersonsPresenter
    @Inject lateinit var state: State

    override fun onCreate( savedInstanceState:Bundle? ) {
        super.onCreate( savedInstanceState )
        ArreevApplication.appComponent.inject(this )
    }

    override fun onCreateView( inflater:LayoutInflater?, container:ViewGroup?, savedInstanceState:Bundle? ) : View? {
        val followersfragmentview = inflater?.inflate( R.layout.followersfragment,container,false );

        val binding = android.databinding.DataBindingUtil.bind<FollowersfragmentBinding>( followersfragmentview  )
        binding.followersfragment = this

        personsPresenter.bind( PersonsAdapter(followersfragmentview ?: View( context ),personsPresenter ) )
        return followersfragmentview
    }

    override fun onDestroyView() {
        super.onDestroyView()
        personsPresenter.unbind()
    }

    override fun onDestroy() {
        super.onDestroy()
        val changingConfigurations = activity?.isChangingConfigurations ?: false
        if (!changingConfigurations) {
            personsPresenter.release()
        }
    }
}
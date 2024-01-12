package com.example.applemarket

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applemarket.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result?.data?.getParcelableExtra(IntentKeys.EXTRA_ITEM, MarketItem::class.java)
            } else {
                result?.data?.getParcelableExtra(IntentKeys.EXTRA_ITEM)
            }

            resultItem?.let {
                dataList.find { it.id == resultItem.id }?.apply {
                    isClicked = resultItem.isClicked
                }.let {
                    adapter.notifyItemChanged(dataList.indexOf(it))
                }
            }
        }
    }

    // 하단의 뒤로가기 버튼을 눌렀을 때 종료 확인 다이얼로그가 뜨는 콜백 함수
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val builder = AlertDialog.Builder(this@MainActivity)
            // 확인 버튼이 눌리면 액티비티 종료
            val listener = DialogInterface.OnClickListener { _, p0 ->
                if (p0 == DialogInterface.BUTTON_POSITIVE) {
                    finish()
                }
            }

            builder.apply {
                setTitle(getString(R.string.dialog_main_exit_title))
                setIcon(R.drawable.img_recycler_message)
                setMessage(getString(R.string.dialog_main_exit_comment))
                // 작업 버튼 설정
                setPositiveButton(getString(R.string.dialog_main_ok_btn), listener)
                setNegativeButton(getString(R.string.dialog_main_cancel_btn), null)
                show()
            }
        }
    }

    // 뷰바인딩을 위한 변수
    private lateinit var binding: ActivityMainBinding
    // 더미 데이터 리스트
    private val dataList = mutableListOf<MarketItem>()

    // 알림 아이디
    private val notificationID = 1
    // 채널 아이디
    private val channelID = "keyword"

    // 어댑터
    private val adapter by lazy {
        ItemAdapter(dataList)
    }

    // 페이드인 애니메이션
    private val fadeIn: Animation by lazy {
        AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in)
    }
    // 페이드아웃 애니메이션
    private val fadeOut: Animation by lazy {
        AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_out)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initialSettings()
    }

    // 초기 세팅 함수
    private fun initialSettings() {
        // 콜백 함수 등록
        this.onBackPressedDispatcher.addCallback(this, callback)

        setMarketItem()
        checkNotificationPermission()
        setRecyclerView()
        setOnClickListener()
    }

    // 클릭 리스너 세팅 함수
    private fun setOnClickListener() {
        // 상단 알림 생성 버튼 클릭 리스너
        binding.ivMainNotificationBtn.setOnClickListener {
            createNotification()
        }

        // 플로팅 액션 버튼 클릭 리스너
        binding.fabMainScrollUp.setOnClickListener {
            binding.recyclerViewMain.smoothScrollToPosition(0)
        }
    }

    // 리사이클러뷰 세팅 함수
    private fun setRecyclerView() {
        binding.recyclerViewMain.adapter = adapter
        binding.recyclerViewMain.layoutManager = LinearLayoutManager(this)

        // 리사이클러뷰 스크롤 리스너
        binding.recyclerViewMain.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var curVisible = false
            // 스크롤 상태가 변화된 후
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // 스크롤이 멈췄으면서 최상단일 때는 플로팅 액션 버튼 숨기기
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && !recyclerView.canScrollVertically(-1)) {
                    binding.fabMainScrollUp.startAnimation(fadeOut)
                    binding.fabMainScrollUp.isVisible = false
                    curVisible = false
                }
            }

            // 스크롤 중일 때
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // 아래로 스크롤을 하고 있는 중일 때 플로팅 액션 버튼 보여주기
                if (dy > 0) {
                    binding.fabMainScrollUp.isVisible = true
                    if (!curVisible) binding.fabMainScrollUp.startAnimation(fadeIn)
                    curVisible = true
                }
            }
        })

        // 리사이클러뷰 아이템 클릭 이벤트 처리
        // 디테일 페이지로 이동
        adapter.itemClick = object : ItemAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {
                var intent = Intent(this@MainActivity, DetailActivity::class.java)
                intent.putExtra(IntentKeys.EXTRA_ITEM, dataList[position])
                resultLauncher.launch(intent)
            }
        }

        // 리사이클러뷰 아이템 롱클릭 이벤트 처리
        // 다이얼로그를 띄운 뒤 확인 버튼이 눌리면 아이템 삭제
        adapter.itemLongClick = object : ItemAdapter.ItemLongClick {
            override fun onLongClick(view: View, position: Int) {
                val builder = AlertDialog.Builder(this@MainActivity)
                // 확인 버튼이 눌리면 아이템 삭제하고 리사이클러뷰에 알려줌
                val listener = DialogInterface.OnClickListener { _, p0 ->
                    if (p0 == DialogInterface.BUTTON_POSITIVE) {
                        dataList.removeAt(position)
                        adapter.notifyDataSetChanged()
                    }
                }

                builder.apply {
                    setTitle(getString(R.string.dialog_main_delete_item_title))
                    setIcon(R.drawable.img_recycler_message)
                    setMessage(getString(R.string.dialog_main_delete_item_comment))
                    // 작업 버튼 설정
                    setPositiveButton(getString(R.string.dialog_main_ok_btn), listener)
                    setNegativeButton(getString(R.string.dialog_main_cancel_btn), null)
                    show()
                }
            }
        }
    }

    // 알림 권한 허용됐는지 체크하고 안 됐다면 설정 화면으로 이동하는 함수
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (NotificationManagerCompat.from(this).areNotificationsEnabled().not()) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                startActivity(intent)
            }
        }
    }

    // 알림을 만드는 함수
    private fun createNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder: NotificationCompat.Builder

        // Android 8.0 이상인지 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 알림 채널 생성
            val channel = NotificationChannel(
                channelID,
                "keyword channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "description text of this channel."
                // 앱 아이콘 알림 표시 노출 여부 설정
                setShowBadge(true)
            }

            // 채널을 NotificationManager에 등록
            notificationManager.createNotificationChannel(channel)

            // 채널을 이용해 builder 초기화
            builder = NotificationCompat.Builder(this, channelID)
        } else {
            // 채널을 이용하지 않고 builder 초기화
            builder = NotificationCompat.Builder(this)
        }

        // 알림 세팅
        builder.run {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle(getString(R.string.notification_main_title))
            setContentText(getString(R.string.notification_main_comment))
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
            setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.notification_main_comment)))
        }

        notificationManager.notify(notificationID, builder.build())
    }

    // 더미 데이터를 세팅하는 함수
    private fun setMarketItem() {
        dataList.add(
            MarketItem(
                1,
                R.drawable.img_sample1,
                "산진 한달된 선풍기 팝니다",
                "이사가서 필요가 없어졌어요 급하게 내놓습니다",
                "대현동",
                1000,
                "서울 서대문구 창천동",
                13,
                25
            )
        )
        dataList.add(
            MarketItem(
                2,
                R.drawable.img_sample2,
                "김치냉장고",
                "이사로인해 내놔요",
                "안마담",
                20000,
                "인천 계양구 귤현동",
                8,
                28
            )
        )
        dataList.add(
            MarketItem(
                3,
                R.drawable.img_sample3,
                "샤넬 카드지갑",
                "고퀄지갑이구요\n사용감이 있어서 싸게 내어둡니다",
                "코코유",
                10000,
                "수성구 범어동",
                23,
                5
            )
        )
        dataList.add(
            MarketItem(
                4,
                R.drawable.img_sample4,
                "금고",
                "금고\n떼서 가져가야함\n대우월드마크센텀\n미국이주관계로 싸게 팝니다",
                "Nicole",
                10000,
                "해운대구 우제2동",
                14,
                17
            )
        )
        dataList.add(
            MarketItem(
                5,
                R.drawable.img_sample5,
                "갤럭시Z플립3 팝니다",
                "갤럭시 Z플립3 그린 팝니다\n항시 케이스 씌워서 썻고 필름 한장챙겨드립니다\n화면에 살짝 스크래치난거 말고 크게 이상은없습니다!",
                "절명",
                150000,
                "연제구 연산제8동",
                22,
                9
            )
        )
        dataList.add(
            MarketItem(
                6,
                R.drawable.img_sample6,
                "프라다 복조리백",
                "까임 오염없고 상태 깨끗합니다\n정품여부모름",
                "미니멀하게",
                50000,
                "수원시 영통구 원천동",
                25,
                16
            )
        )
        dataList.add(
            MarketItem(
                7,
                R.drawable.img_sample7,
                "울산 동해오션뷰 60평 복층 펜트하우스 1일 숙박권 펜션 힐링 숙소 별장",
                "울산 동해바다뷰 60평 복층 펜트하우스 1일 숙박권\n(에어컨이 없기에 낮은 가격으로 변경했으며 8월 초 가장 더운날 다녀가신 분 경우 시원했다고 잘 지내다 가셨습니다)\n1. 인원: 6명 기준입니다. 1인 10,000원 추가요금\n2. 장소: 북구 블루마시티, 32-33층\n3. 취사도구, 침구류, 세면도구, 드라이기 2개, 선풍기 4대 구비\n4. 예약방법: 예약금 50,000원 하시면 저희는 명함을 드리며 입실 오전 잔금 입금하시면 저희는 동.호수를 알려드리며 고객님은 예약자분 신분증 앞면 주민번호 뒷자리 가리시거나 지우시고 문자로 보내주시면 저희는 카드키를 우편함에 놓아 둡니다.\n5. 33층 옥상 야외 테라스 있음, 가스버너 있음\n6. 고기 굽기 가능\n7. 입실 오후 3시, 오전 11시 퇴실, 정리, 정돈 , 밸브 잠금 부탁드립니다.\n8. 층간소음 주의 부탁드립니다.\n9. 방3개, 화장실3개, 비데 3개\n10. 저희 집안이 쓰는 별장입니다.",
                "굿리치",
                150000,
                "남구 옥동",
                142,
                54
            )
        )
        dataList.add(
            MarketItem(
                8,
                R.drawable.img_sample8,
                "샤넬 탑핸들 가방",
                "샤넬 트랜디 CC 탑핸들 스몰 램스킨 블랙 금장 플랩백 !\n\n색상 : 블랙\n사이즈 : 25.5cm * 17.5cm * 8cm\n구성 : 본품, 더스트\n\n급하게 돈이 필요해서 팝니다 ㅠ ㅠ,,",
                "난쉽",
                180000,
                "동래구 온천제2동",
                31,
                7
            )
        )
        dataList.add(
            MarketItem(
                9,
                R.drawable.img_sample9,
                "4행정 엔진분무기 판매합니다.",
                "3년전에 사서 한번 사용하고 그대로 둔 상태입니다. 요즘 사용은 안해봤습니다. 그래서 저렴하게 내 놓습니다. 중고라 반품은 어렵습니다.\n",
                "알뜰한",
                30000,
                "원주시 명륜2동",
                7,
                28
            )
        )
        dataList.add(
            MarketItem(
                10,
                R.drawable.img_sample10,
                "셀린느 버킷 가방",
                "22년 신세계 대전 구매입니당\n셀린느 버킷백\n구매해서 몇번사용했어요\n까짐 스크래치 없습니다.\n타지역에서 보내는거라 택배로 진행합니당!",
                "똑태현",
                190000,
                "중구 동화동",
                40,
                6
            )
        )
    }
}
# Please install OpenAI SDK first: `pip3 install openai`
import sys
from openai import OpenAI

if len(sys.argv) < 2:
    print("Please provide the content as a command line argument.")
    sys.exit(1)

content = sys.argv[1]

client = OpenAI(api_key="sk-f7241070a52a4623ac07b56ceb262ec2", base_url="https://api.deepseek.com")

response = client.chat.completions.create(
    model="deepseek-chat",
    messages=[
        {"role": "system", "content": "You are a helpful assistant"},
        {"role": "user", "content": "对于我输入的内容：'" + content + "'，请帮我分析这个内容的商品属于以下之中哪一个类别：服装与配饰-男装，服装与配饰-女装，服装与配饰-配饰，电子产品-手机与配件，电子产品-电脑与配件，电子产品-家用电器，电子产品-摄影与摄像，家居与生活-家具，家居与生活-家纺，家居与生活-厨房用品，家居与生活-清洁用品，美妆与个人护理-护肤品，美妆与个人护理-彩妆，美妆与个人护理-个人护理，食品与饮料-零食，食品与饮料-饮料，食品与饮料-生鲜食品，运动与户外-运动服装，运动与户外-健身器材，运动与户外-户外装备，母婴用品-婴儿服装，母婴用品-婴儿用品，母婴用品-孕妇用品，图书与文具-图书，图书与文具-文具，汽车与配件-汽车，汽车与配件-汽车配件，宠物用品-宠物食品，宠物用品-宠物用品，礼品与定制-礼品，礼品与定制-定制产品。注意：请不要分析，只返回其可能属于的一种商品类别。"},
    ],
    stream=False
)

print(response.choices[0].message.content)